package com.petcare.app.ui.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import com.petcare.app.data.db.PetCareDatabase
import com.petcare.app.data.db.dao.DiaryDao
import com.petcare.app.data.db.dao.HealthRecordDao
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.DiaryEntry
import com.petcare.app.data.db.entity.HealthRecord
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ── Eventos de UI para o ProfileScreen ────────────────────────────────────────
sealed class ProfileUiEvent {
    object ExportSuccess                          : ProfileUiEvent()
    data class ExportError(val msg: String)       : ProfileUiEvent()
    object ImportSuccess                          : ProfileUiEvent()
    data class ImportError(val msg: String)       : ProfileUiEvent()
    object DeleteSuccess                          : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs            : UserPreferencesRepository,
    private val db               : PetCareDatabase,
    private val petDao           : PetDao,
    private val reminderDao      : ReminderDao,
    private val diaryDao         : DiaryDao,
    private val healthRecordDao  : HealthRecordDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // ── Nome do usuário ───────────────────────────────────────────────────────
    val userName = prefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setUserName(name: String) {
        viewModelScope.launch { prefs.setUserName(name.trim()) }
    }

    // ── Eventos one-shot para a UI ────────────────────────────────────────────
    private val _events = MutableSharedFlow<ProfileUiEvent>()
    val events: SharedFlow<ProfileUiEvent> = _events.asSharedFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // Exportar backup via SAF
    //
    // PROBLEMA WAL: Room usa WAL mode por padrão. O arquivo petcare.db pode
    // estar praticamente vazio (só header) enquanto todos os dados — inclusive
    // os CREATE TABLE — ficam em petcare.db-wal. PRAGMA wal_checkpoint(FULL)
    // com conexão ativa do Room não funciona (ele deteta readers abertos e
    // desiste silenciosamente). Copiar só o .db produz um arquivo sem tabelas.
    //
    // SOLUÇÃO: copiar o .db + .db-wal para um diretório temporário, abrir a
    // cópia com SQLiteDatabase nativo (merge do WAL é automático no open),
    // forçar PRAGMA wal_checkpoint(TRUNCATE) lá — agora sem conexões do Room
    // interferindo — fechar e só então copiar o arquivo resultante para o SAF.
    // O arquivo exportado é um SQLite limpo, auto-suficiente, sem WAL.
    // ─────────────────────────────────────────────────────────────────────────
    fun exportBackup(contentResolver: ContentResolver, treeUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val srcDb  = context.getDatabasePath("petcare.db")
                val srcWal = File(srcDb.parent!!, "petcare.db-wal")
                val srcShm = File(srcDb.parent!!, "petcare.db-shm")

                if (!srcDb.exists()) {
                    _events.emit(ProfileUiEvent.ExportError(
                        "Banco de dados não encontrado. Adicione um pet antes de exportar."))
                    return@launch
                }

                // 1. Copia .db (e .db-wal se existir) para diretório temporário
                val tempDir = File(context.cacheDir, "petcare_export_tmp")
                tempDir.deleteRecursively()
                tempDir.mkdirs()
                val tempDb  = File(tempDir, "petcare.db")
                val tempWal = File(tempDir, "petcare.db-wal")

                srcDb.copyTo(tempDb, overwrite = true)
                if (srcWal.exists()) srcWal.copyTo(tempWal, overwrite = true)
                // .db-shm é recriado automaticamente; não é necessário copiar

                // 2. Abre a cópia com SQLite nativo — merge do WAL é automático
                //    Força checkpoint TRUNCATE: aqui não há conexão do Room
                //    interferindo, então o checkpoint completa de verdade.
                val exportDb = SQLiteDatabase.openDatabase(
                    tempDb.absolutePath, null, SQLiteDatabase.OPEN_READWRITE,
                )
                exportDb.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).close()
                exportDb.close()
                // Após o close, tempDb é um .db limpo e auto-suficiente (sem WAL)

                // 3. Cria petcare_backup.db dentro da pasta escolhida pelo usuário
                val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
                val docUri = DocumentsContract.createDocument(
                    contentResolver, parentUri,
                    "application/octet-stream", "petcare_backup.db",
                ) ?: run {
                    tempDir.deleteRecursively()
                    _events.emit(ProfileUiEvent.ExportError(
                        "Não foi possível criar o arquivo na pasta selecionada."))
                    return@launch
                }

                // 4. Copia o arquivo limpo para o SAF
                contentResolver.openOutputStream(docUri)?.use { out ->
                    tempDb.inputStream().use { it.copyTo(out) }
                }

                tempDir.deleteRecursively()
                _events.emit(ProfileUiEvent.ExportSuccess)
            } catch (e: Exception) {
                _events.emit(ProfileUiEvent.ExportError(e.localizedMessage ?: "Erro ao exportar."))
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Importar backup via SAF
    // merge = true  → mantém dados existentes e adiciona os do backup
    // merge = false → apaga tudo e substitui pelos dados do backup
    //
    // Estratégia: lê o .db do backup com SQLiteDatabase nativo (sem Room),
    // re-insere via DAOs com id=0 para gerar novos IDs e preservar FK chains.
    // ─────────────────────────────────────────────────────────────────────────
    fun importBackup(contentResolver: ContentResolver, fileUri: Uri, merge: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Salva backup em arquivo temporário
                val tempFile = File(context.cacheDir, "petcare_import_temp.db")
                contentResolver.openInputStream(fileUri)?.use { input ->
                    tempFile.outputStream().use { input.copyTo(it) }
                } ?: run {
                    _events.emit(ProfileUiEvent.ImportError("Não foi possível ler o arquivo."))
                    return@launch
                }

                val src = SQLiteDatabase.openDatabase(
                    tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY,
                )

                // 2. Se substituir, limpa todos os dados antes
                if (!merge) db.clearAllTables()

                // 3. Pets: insere com id=0 e mapeia oldId → newId para preservar FKs
                val petIdMap = mutableMapOf<Long, Long>()
                src.rawQuery("SELECT * FROM pets", null).use { c ->
                    while (c.moveToNext()) {
                        val oldId = c.getLong(c.getColumnIndexOrThrow("id"))
                        val newId = petDao.insertPet(Pet(
                            id                = 0,
                            name              = c.getString(c.getColumnIndexOrThrow("name")),
                            species           = c.getString(c.getColumnIndexOrThrow("species")),
                            breed             = c.getString(c.getColumnIndexOrThrow("breed")),
                            sex               = c.getString(c.getColumnIndexOrThrow("sex")),
                            isCastrated       = c.getInt(c.getColumnIndexOrThrow("isCastrated")) != 0,
                            birthDate         = c.getString(c.getColumnIndexOrThrow("birthDate")),
                            approximateAge    = c.getString(c.getColumnIndexOrThrow("approximateAge")),
                            weightKg          = c.getDouble(c.getColumnIndexOrThrow("weightKg")),
                            bloodType         = c.getString(c.getColumnIndexOrThrow("bloodType")),
                            allergies         = c.getString(c.getColumnIndexOrThrow("allergies")),
                            chronicConditions = c.getString(c.getColumnIndexOrThrow("chronicConditions")),
                            microchip         = c.getString(c.getColumnIndexOrThrow("microchip")),
                            notes             = c.getString(c.getColumnIndexOrThrow("notes")),
                            vetName           = c.getString(c.getColumnIndexOrThrow("vetName")),
                            vetPhone          = c.getString(c.getColumnIndexOrThrow("vetPhone")),
                            photoPath         = c.getString(c.getColumnIndexOrThrow("photoPath")),
                            createdAt         = c.getLong(c.getColumnIndexOrThrow("createdAt")),
                        ))
                        petIdMap[oldId] = newId
                    }
                }

                // 4. Lembretes
                src.rawQuery("SELECT * FROM reminders", null).use { c ->
                    while (c.moveToNext()) {
                        val newPetId = petIdMap[c.getLong(c.getColumnIndexOrThrow("petId"))]
                            ?: continue
                        reminderDao.insertReminder(Reminder(
                            id             = 0,
                            petId          = newPetId,
                            title          = c.getString(c.getColumnIndexOrThrow("title")),
                            category       = c.getString(c.getColumnIndexOrThrow("category")),
                            dateTimeMillis = c.getLong(c.getColumnIndexOrThrow("dateTimeMillis")),
                            recurrence     = c.getString(c.getColumnIndexOrThrow("recurrence")),
                            isCompleted    = c.getInt(c.getColumnIndexOrThrow("isCompleted")) != 0,
                            notes          = c.getString(c.getColumnIndexOrThrow("notes")),
                            notificationId = c.getInt(c.getColumnIndexOrThrow("notificationId")),
                        ))
                    }
                }

                // 5. Diário
                src.rawQuery("SELECT * FROM diary_entries", null).use { c ->
                    while (c.moveToNext()) {
                        val newPetId = petIdMap[c.getLong(c.getColumnIndexOrThrow("petId"))]
                            ?: continue
                        diaryDao.insertEntry(DiaryEntry(
                            id         = 0,
                            petId      = newPetId,
                            photoPath  = c.getString(c.getColumnIndexOrThrow("photoPath")),
                            caption    = c.getString(c.getColumnIndexOrThrow("caption")),
                            dateMillis = c.getLong(c.getColumnIndexOrThrow("dateMillis")),
                        ))
                    }
                }

                // 6. Registros de saúde
                src.rawQuery("SELECT * FROM health_records", null).use { c ->
                    while (c.moveToNext()) {
                        val newPetId = petIdMap[c.getLong(c.getColumnIndexOrThrow("petId"))]
                            ?: continue
                        healthRecordDao.insertRecord(HealthRecord(
                            id                     = 0,
                            petId                  = newPetId,
                            type                   = c.getString(c.getColumnIndexOrThrow("type")),
                            vaccineName            = c.getString(c.getColumnIndexOrThrow("vaccineName")),
                            vaccineLot             = c.getString(c.getColumnIndexOrThrow("vaccineLot")),
                            nextDoseDate           = c.getString(c.getColumnIndexOrThrow("nextDoseDate")),
                            consultationReason     = c.getString(c.getColumnIndexOrThrow("consultationReason")),
                            diagnosis              = c.getString(c.getColumnIndexOrThrow("diagnosis")),
                            vetInstructions        = c.getString(c.getColumnIndexOrThrow("vetInstructions")),
                            weightKg               = c.getDouble(c.getColumnIndexOrThrow("weightKg")),
                            feedingType            = c.getString(c.getColumnIndexOrThrow("feedingType")),
                            feedingAmountGrams     = c.getDouble(c.getColumnIndexOrThrow("feedingAmountGrams")),
                            feedingSchedule        = c.getString(c.getColumnIndexOrThrow("feedingSchedule")),
                            medicationName         = c.getString(c.getColumnIndexOrThrow("medicationName")),
                            medicationDosage       = c.getString(c.getColumnIndexOrThrow("medicationDosage")),
                            medicationFrequency    = c.getString(c.getColumnIndexOrThrow("medicationFrequency")),
                            medicationDurationDays = c.getInt(c.getColumnIndexOrThrow("medicationDurationDays")),
                            dateMillis             = c.getLong(c.getColumnIndexOrThrow("dateMillis")),
                            notes                  = c.getString(c.getColumnIndexOrThrow("notes")),
                        ))
                    }
                }

                src.close()
                tempFile.delete()
                _events.emit(ProfileUiEvent.ImportSuccess)
            } catch (e: Exception) {
                _events.emit(ProfileUiEvent.ImportError(
                    e.localizedMessage ?: "Erro ao importar backup."))
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Apagar todos os dados (confirmação dupla na UI)
    // Limpa todas as tabelas Room + reseta nome do usuário.
    // Preferências de tema e onboarding são preservadas.
    // ─────────────────────────────────────────────────────────────────────────
    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.clearAllTables()
                prefs.setUserName("")
                _events.emit(ProfileUiEvent.DeleteSuccess)
            } catch (_: Exception) { /* raramente falha */ }
        }
    }
}
