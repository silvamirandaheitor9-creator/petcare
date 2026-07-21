package com.petcare.app.ui.screen.onboarding

import androidx.annotation.DrawableRes

/**
 * Dados de uma página do onboarding.
 *
 * [imageRes] null apenas para as telas de tema, termos e permissões.
 * [isThemePage] / [isTermsPage] / [isPermissionsPage] ativam layouts especiais.
 */
data class OnboardingPageData(
    @DrawableRes val imageRes: Int?,
    val title: String,
    val subtitle: String,
    val isThemePage: Boolean = false,
    val isTermsPage: Boolean = false,
    val isPermissionsPage: Boolean = false,
)
