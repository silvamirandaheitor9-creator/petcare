package com.petcare.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.HealthRecordDao
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.entity.HealthRecord
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel do detalhe do pet (SPEC §12 — Partes 1 e 2) ──────────────────
// Carrega o pet por ID e expõe as listas de registros de saúde por tipo.
// Cada Flow é convertido em StateFlow para consumo eficiente no Compose.

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val petDao: PetDao,
    private val healthRecordDao: HealthRecordDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** ID do pet passado como argumento de navegação. */
    val petId: Long = checkNotNull(savedStateHandle["petId"])

    /** Dados completos do pet — null enquanto o banco carrega. */
    val pet: StateFlow<Pet?> = petDao.getPetById(petId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Vacinas do pet ordenadas por data (mais recente primeiro). */
    val vaccines: StateFlow<List<HealthRecord>> =
        healthRecordDao.getRecordsByPetAndType(petId, "vaccine")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Medicamentos do pet ordenados por data (mais recente primeiro). */
    val medications: StateFlow<List<HealthRecord>> =
        healthRecordDao.getRecordsByPetAndType(petId, "medication")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Consultas do pet ordenadas por data (mais recente primeiro). */
    val consultations: StateFlow<List<HealthRecord>> =
        healthRecordDao.getRecordsByPetAndType(petId, "consultation")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Pesagens do pet — usadas para o gráfico e a lista da sub-aba Peso. */
    val weights: StateFlow<List<HealthRecord>> =
        healthRecordDao.getRecordsByPetAndType(petId, "weight")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Insere um novo registro de saúde no banco. */
    fun insertRecord(record: HealthRecord) {
        viewModelScope.launch { healthRecordDao.insertRecord(record) }
    }

    /** Remove um registro de saúde do banco. */
    fun deleteRecord(record: HealthRecord) {
        viewModelScope.launch { healthRecordDao.deleteRecord(record) }
    }
}
