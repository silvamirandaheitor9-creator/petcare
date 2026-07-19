package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Grava um novo pet no Room (SPEC §11 — formulário "Novo Pet").
 * A validação de campos acontece na própria tela (NewPetScreen); este
 * ViewModel só persiste o [Pet] já validado.
 *
 * [photoPath] guarda o caminho da foto de perfil já cortada (SPEC §11 —
 * parte 2). Fica no ViewModel — não em `rememberSaveable` da tela — porque é
 * essa mesma instância (escopada à entrada "new_pet" do NavGraph) que recebe
 * o resultado do `PetPhotoEditorScreen`, uma rota separada empilhada por
 * cima; ver `PetCareNavGraph`.
 */
@HiltViewModel
class NewPetViewModel @Inject constructor(
    private val petDao: PetDao,
) : ViewModel() {

    private val _photoPath = MutableStateFlow<String?>(null)
    val photoPath: StateFlow<String?> = _photoPath

    fun setPhotoPath(path: String?) {
        _photoPath.value = path
    }

    /**
     * Trava de duplo-toque: MutableStateFlow.value é lido/escrito de forma
     * síncrona na main thread, portanto o segundo toque já encontra true
     * antes de qualquer suspensão de coroutine.
     *
     * O valor intencionalmente NÃO é resetado para false após o insert:
     * onSaved() navega para fora da tela imediatamente, e a tela destruída
     * leva o ViewModel junto (escopo do NavGraph). Manter true elimina
     * qualquer janela de corrida residual.
     */
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun savePet(pet: Pet, onSaved: () -> Unit) {
        // Guard síncrono: qualquer toque subsequente retorna aqui
        // antes de entrar na coroutine.
        if (_isSaving.value) return
        _isSaving.value = true

        viewModelScope.launch {
            petDao.insertPet(pet)
            // Navega imediatamente — sem overlay intermediário que cria janela
            // para toques duplicados. A tela (e este ViewModel) são destruídos
            // pelo NavController antes que um segundo toque possa chegar.
            onSaved()
        }
    }
}
