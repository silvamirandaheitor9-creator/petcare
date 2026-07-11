package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Limite atual de pets do plano gratuito (SPEC §18 — expansível via anúncio rewarded). */
const val PET_LIMIT_FREE = 10

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val petDao: PetDao,
) : ViewModel() {

    /** Lista completa de pets, ordenada por data de criação (mais recente primeiro). */
    val pets: StateFlow<List<Pet>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Contagem total de pets cadastrados — usada no badge "X/10" do título. */
    val petCount: StateFlow<Int> = petDao.getPetCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
