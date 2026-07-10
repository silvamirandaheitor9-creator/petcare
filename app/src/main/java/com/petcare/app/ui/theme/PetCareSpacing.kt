package com.petcare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Grade de espaçamento 8dp conforme SPEC seção 3 ───────────────────────────
// Usar estes tokens em todos os paddings/margins do app em vez de valores
// soltos (magic numbers), para garantir consistência em 100% das telas.
@Immutable
data class PetCareSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 16.dp,
    val md: Dp = 24.dp,
    val lg: Dp = 32.dp,
    val xl: Dp = 40.dp,
    val xxl: Dp = 48.dp,
)

val LocalPetCareSpacing = staticCompositionLocalOf { PetCareSpacing() }

/**
 * Acesso conveniente: `MaterialTheme.spacing.sm` dentro de qualquer Composable
 * envolvido por `PetCareTheme`. Extension sobre `MaterialTheme` (em vez de um
 * objeto `PetCareTheme` próprio) para não colidir com a função Composable
 * `PetCareTheme(...)` definida em PetCareTheme.kt.
 */
val MaterialTheme.spacing: PetCareSpacing
    @Composable get() = LocalPetCareSpacing.current
