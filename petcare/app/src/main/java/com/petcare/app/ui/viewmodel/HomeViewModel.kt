package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import com.petcare.app.data.db.dao.HealthRecordDao
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petDao: PetDao,
    private val reminderDao: ReminderDao,
    private val healthRecordDao: HealthRecordDao,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    /** Lista completa de pets, mais recentes primeiro. ImmutableList → estável para Compose. */
    val pets: StateFlow<ImmutableList<Pet>> = petDao.getAllPets()
        .map { it.toPersistentList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())

    /** Contagem total de pets cadastrados. */
    val petCount: StateFlow<Int> = petDao.getPetCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Nome do usuário salvo no DataStore. */
    val userName: StateFlow<String> = prefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /**
     * Data da próxima VACINA — combina duas fontes:
     * 1. Lembretes com category == "vacina" (criados via aba Lembretes)
     * 2. nextDoseDate de HealthRecords tipo "vaccine" (adicionados em PetDetailScreen)
     *
     * Retorna a data mais próxima no futuro, formatada "dd/MM". Null se nenhuma.
     */
    val nextVaccineDate: StateFlow<String?> = combine(
        reminderDao.getPendingReminders(),
        healthRecordDao.getVaccinesWithNextDose(),
    ) { reminders, vaccineRecords ->
        val now = System.currentTimeMillis()

        val fromReminders = reminders
            .filter { it.category == "vacina" && it.dateTimeMillis > now }
            .minByOrNull { it.dateTimeMillis }
            ?.dateTimeMillis

        val fromRecords = vaccineRecords
            .mapNotNull { parseNextDoseDateToMillis(it.nextDoseDate) }
            .filter { it > now }
            .minOrNull()

        val earliest = when {
            fromReminders == null -> fromRecords
            fromRecords   == null -> fromReminders
            else                  -> minOf(fromReminders, fromRecords)
        }
        earliest?.toShortDate()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Data da próxima CONSULTA (category == "consulta") dos lembretes.
     * Formatada como "dd/MM". Null se nenhuma no futuro.
     */
    val nextConsultDate: StateFlow<String?> = reminderDao.getPendingReminders()
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.category == "consulta" && it.dateTimeMillis > now }
                .minByOrNull { it.dateTimeMillis }
                ?.dateTimeMillis
                ?.toShortDate()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

// ─── Helpers privados ─────────────────────────────────────────────────────────

/** Analisa "dd/MM/yyyy" (formato BR) → millis UTC. Retorna null se inválido ou vazio. */
private fun parseNextDoseDateToMillis(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).parse(dateStr)?.time
    } catch (_: Exception) { null }
}

/** Formata millis como "dd/MM" em pt-BR (ex.: "15/07"). */
private fun Long.toShortDate(): String =
    SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(Date(this))
