package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Grava um novo pet no Room (SPEC §11 — formulário "Novo Pet").
 * A validação de campos acontece na própria tela (NewPetScreen); este
 * ViewModel só persiste o [Pet] já validado.
 */
@HiltViewModel
class NewPetViewModel @Inject constructor(
    private val petDao: PetDao,
) : ViewModel() {

    fun savePet(pet: Pet, onSaved: () -> Unit) {
        viewModelScope.launch {
            petDao.insertPet(pet)
            onSaved()
        }
    }
}
