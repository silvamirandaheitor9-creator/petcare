package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class ReminderGroup { HOJE, AMANHA, ESTA_SEMANA, HISTORICO }

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderDao: ReminderDao,
    private val petDao: PetDao,
) : ViewModel() {

    private val allReminders: StateFlow<List<Reminder>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pets: StateFlow<List<Pet>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedPetId = MutableStateFlow<Long?>(null)
    val selectedPetId: StateFlow<Long?> = _selectedPetId.asStateFlow()

    private val _historicoExpanded = MutableStateFlow(false)
    val historicoExpanded: StateFlow<Boolean> = _historicoExpanded.asStateFlow()

    val groupedReminders: StateFlow<Map<ReminderGroup, List<Reminder>>> =
        combine(allReminders, _selectedPetId) { reminders, petId ->
            val filtered = if (petId == null) reminders else reminders.filter { it.petId == petId }
            groupByDate(filtered)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun selectPet(petId: Long?) { _selectedPetId.value = petId }

    fun toggleHistorico() { _historicoExpanded.value = !_historicoExpanded.value }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminderDao.deleteReminder(reminder) }
    }

    fun toggleCompleted(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    private fun groupByDate(reminders: List<Reminder>): Map<ReminderGroup, List<Reminder>> {
        val now = Calendar.getInstance()

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfTomorrow = startOfToday + 86_400_000L
        val startOfDayAfterTomorrow = startOfTomorrow + 86_400_000L

        val result = LinkedHashMap<ReminderGroup, MutableList<Reminder>>()
        result[ReminderGroup.HOJE] = mutableListOf()
        result[ReminderGroup.AMANHA] = mutableListOf()
        result[ReminderGroup.ESTA_SEMANA] = mutableListOf()
        result[ReminderGroup.HISTORICO] = mutableListOf()

        for (r in reminders) {
            when {
                r.dateTimeMillis < startOfToday ->
                    result[ReminderGroup.HISTORICO]!!.add(r)
                r.dateTimeMillis < startOfTomorrow ->
                    result[ReminderGroup.HOJE]!!.add(r)
                r.dateTimeMillis < startOfDayAfterTomorrow ->
                    result[ReminderGroup.AMANHA]!!.add(r)
                else ->
                    result[ReminderGroup.ESTA_SEMANA]!!.add(r)
            }
        }

        result[ReminderGroup.HISTORICO]!!.sortByDescending { it.dateTimeMillis }

        return result.filter { it.value.isNotEmpty() }
    }
}
