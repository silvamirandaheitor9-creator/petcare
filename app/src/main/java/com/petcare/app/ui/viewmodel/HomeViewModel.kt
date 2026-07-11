package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import com.petcare.app.ui.screen.main.MelTip
import com.petcare.app.ui.screen.main.MelTips
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petDao: PetDao,
    private val reminderDao: ReminderDao,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    /** Lista completa de pets, ordenada por data de criação (mais recente primeiro). */
    val pets: StateFlow<List<Pet>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    /**
     * Dica diária da Mel — determinística por dia do ano, baseada na espécie
     * do primeiro pet cadastrado (ou dica geral se não houver pets).
     */
    val dailyTip: StateFlow<MelTip> = pets
        .map { list ->
            val species = list.firstOrNull()?.species
            MelTips.pickForDay(species)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MelTips.pickForDay(null),
        )
}
