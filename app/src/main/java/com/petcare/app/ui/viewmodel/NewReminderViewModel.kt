package com.petcare.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NewReminderViewModel @Inject constructor(
    private val reminderDao: ReminderDao,
    private val petDao: PetDao,
) : ViewModel() {

    val pets: StateFlow<List<Pet>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var editingReminderId: Long by mutableLongStateOf(-1L)
        private set

    var title by mutableStateOf("")
    var selectedPetId by mutableLongStateOf(-1L)
    var category by mutableStateOf("vacina")
    var dateTimeMillis by mutableLongStateOf(defaultDateTimeMillis())
    var recurrence by mutableStateOf("none")
    var notes by mutableStateOf("")

    var isSaving by mutableStateOf(false)
        private set

    val isValid: Boolean
        get() = title.isNotBlank() && selectedPetId > 0

    fun loadReminder(reminderId: Long) {
        if (reminderId <= 0L || editingReminderId == reminderId) return
        editingReminderId = reminderId
        viewModelScope.launch {
            val r = reminderDao.getReminderById(reminderId) ?: return@launch
            title = r.title
            selectedPetId = r.petId
            category = r.category
            dateTimeMillis = r.dateTimeMillis
            recurrence = r.recurrence
            notes = r.notes
        }
    }

    fun saveReminder(onDone: () -> Unit) {
        if (!isValid || isSaving) return
        isSaving = true
        viewModelScope.launch {
            if (editingReminderId > 0) {
                val existing = reminderDao.getReminderById(editingReminderId)
                if (existing != null) {
                    reminderDao.updateReminder(
                        existing.copy(
                            title = title.trim(),
                            petId = selectedPetId,
                            category = category,
                            dateTimeMillis = dateTimeMillis,
                            recurrence = recurrence,
                            notes = notes.trim(),
                        )
                    )
                }
            } else {
                reminderDao.insertReminder(
                    Reminder(
                        title = title.trim(),
                        petId = selectedPetId,
                        category = category,
                        dateTimeMillis = dateTimeMillis,
                        recurrence = recurrence,
                        notes = notes.trim(),
                    )
                )
            }
            isSaving = false
            onDone()
        }
    }

    private fun defaultDateTimeMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
