package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.entity.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Limite base do plano gratuito (SPEC §18). */
const val PET_LIMIT_FREE  = 10
/** Vagas extras liberadas a cada rewarded ad assistido (SPEC §18.4). */
const val PET_LIMIT_BONUS = 5

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val petDao : PetDao,
    private val prefs  : UserPreferencesRepository,
) : ViewModel() {

    /** Lista completa de pets, ordenada por data de criação (mais recente primeiro). */
    val pets: StateFlow<ImmutableList<Pet>> = petDao.getAllPets()
        .map { it.toPersistentList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())

    /** Contagem total de pets — usada no badge "X/N" do título. */
    val petCount: StateFlow<Int> = petDao.getPetCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /**
     * Limite dinâmico: 10 (base) + extraSlotsCount acumulado via rewarded ads.
     * Cada anúncio assistido soma +5: 10 → 15 → 20 → 25...
     */
    val petLimit: StateFlow<Int> = prefs.extraSlotsCount
        .map { extras -> PET_LIMIT_FREE + extras }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PET_LIMIT_FREE)

    /**
     * Chamada quando o rewarded ad é concluído (SPEC §18.4).
     * Incrementa o contador de vagas em +5; petLimit sobe automaticamente via Flow.
     */
    fun unlockExtraSlots() {
        viewModelScope.launch { prefs.addExtraSlots(PET_LIMIT_BONUS) }
    }

    /**
     * Exclui um pet diretamente da lista (Meus Pets).
     * Move o botão de exclusão da PetDetailScreen para os cards — SPEC §8.
     */
    fun deletePetFromList(pet: Pet) {
        viewModelScope.launch { petDao.deletePet(pet) }
    }
}
