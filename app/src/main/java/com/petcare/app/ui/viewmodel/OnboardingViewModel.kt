package com.petcare.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    // ── Seleção de tema (tela 6) ─────────────────────────────────────────────
    // Estado local para feedback imediato na UI.
    // Salva no DataStore em tempo real para que ThemeViewModel aplique a troca ao vivo.
    private val _selectedDark = MutableStateFlow(false)
    val selectedDark: StateFlow<Boolean> = _selectedDark.asStateFlow()

    fun selectTheme(dark: Boolean) {
        _selectedDark.value = dark
        viewModelScope.launch { prefs.setDarkTheme(dark) }
    }

    // ── Conclusão do onboarding (chamado ao aceitar Termos, tela 7) ──────────
    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingDone(true)
            prefs.setTermsAccepted(true)
        }
    }
}
