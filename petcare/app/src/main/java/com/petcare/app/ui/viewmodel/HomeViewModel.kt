package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petDao: PetDao,
    private val reminderDao: ReminderDao,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    /** Lista completa de pets, ordenada por data de criação (mais recente primeiro).
     *  ImmutableList → compilador do Compose reconhece como estável. */
    val pets: StateFlow<ImmutableList<Pet>> = petDao.getAllPets()
        .map { it.toPersistentList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())

    /** Contagem total de pets cadastrados. */
    val petCount: StateFlow<Int> = petDao.getPetCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Nome do usuário salvo no DataStore (pode ser vazio se ainda não preenchido). */
    val userName: StateFlow<String> = prefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /**
     * Próximo lembrete de VACINA (category == "vacina") ainda pendente e no futuro.
     * Null quando não há nenhum agendado.
     */
    val nextVaccineReminder: StateFlow<Reminder?> = reminderDao.getPendingReminders()
        .map { list ->
            val now = System.currentTimeMillis()
            list.firstOrNull { it.category == "vacina" && it.dateTimeMillis > now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Próximo lembrete de CONSULTA (category == "consulta") ainda pendente e no futuro.
     * Null quando não há nenhum agendado.
     */
    val nextConsultationReminder: StateFlow<Reminder?> = reminderDao.getPendingReminders()
        .map { list ->
            val now = System.currentTimeMillis()
            list.firstOrNull { it.category == "consulta" && it.dateTimeMillis > now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
