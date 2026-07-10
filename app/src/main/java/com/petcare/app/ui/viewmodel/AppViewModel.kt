package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import com.petcare.app.debug.StartupTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    val isOnboardingDone: StateFlow<Boolean> = prefs.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        StartupTimer.mark("AppViewModel.init start")
        viewModelScope.launch {
            // Aguarda DataStore estar pronto (seção 4: navegação só ocorre quando
            // animação mínima terminar E carregamento real concluir)
            var first = true
            prefs.isOnboardingDone.collect {
                if (first) {
                    StartupTimer.mark("AppViewModel: first DataStore emission received")
                    first = false
                }
                _isReady.value = true
            }
        }
    }
}
