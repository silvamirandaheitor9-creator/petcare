package com.petcare.app.ui.screen.onboarding

import androidx.annotation.DrawableRes

/**
 * Dados de uma página do onboarding.
 *
 * [imageRes] null apenas para as telas de tema (6) e termos (7).
 * [isThemePage] / [isTermsPage] ativam layouts especiais nas tarefas 3 e 4.
 */
data class OnboardingPageData(
    @DrawableRes val imageRes: Int?,
    val title: String,
    val subtitle: String,
    val isThemePage: Boolean = false,
    val isTermsPage: Boolean = false,
)
