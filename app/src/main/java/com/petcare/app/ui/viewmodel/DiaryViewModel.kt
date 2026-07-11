package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.DiaryDao
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.entity.DiaryEntry
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryDao: DiaryDao,
    private val petDao: PetDao,
) : ViewModel() {

    /** Todas as entradas do diário, mais recentes primeiro. */
    val entries: StateFlow<List<DiaryEntry>> = diaryDao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Pets cadastrados — usados para o filtro por pet e para exibir o nome na timeline. */
    val pets: StateFlow<List<Pet>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Exclui uma entrada do diário (SPEC 9.6). */
    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch { diaryDao.deleteEntry(entry) }
    }

    /** Cria uma nova entrada a partir da foto editada (SPEC 9.8-9.11). */
    fun addEntry(petId: Long, photoPath: String, caption: String) {
        viewModelScope.launch {
            diaryDao.insertEntry(
                DiaryEntry(petId = petId, photoPath = photoPath, caption = caption.take(140)),
            )
        }
    }
}
