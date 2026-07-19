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
    private val _selectedDark = MutableStateFlow(false)
    val selectedDark: StateFlow<Boolean> = _selectedDark.asStateFlow()

    fun selectTheme(dark: Boolean) {
        _selectedDark.value = dark
        viewModelScope.launch { prefs.setDarkTheme(dark) }
    }

    // ── Aceite dos termos (tela 7) ───────────────────────────────────────────
    // Estado local do checkbox durante o onboarding.
    // Salvo definitivamente em prefs.setTermsAccepted() só ao chamar completeOnboarding().
    private val _termsChecked = MutableStateFlow(false)
    val termsChecked: StateFlow<Boolean> = _termsChecked.asStateFlow()

    fun setTermsChecked(checked: Boolean) {
        _termsChecked.value = checked
    }

    // ── Conclusão do onboarding (chamado ao pressionar "Aceitar e continuar") ─
    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingDone(true)
            prefs.setTermsAccepted(true)
        }
    }
}
