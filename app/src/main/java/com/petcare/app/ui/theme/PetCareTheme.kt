package com.petcare.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Cores conforme SPEC seção 3 ──────────────────────────────────────────────
val OrangePrimary    = Color(0xFFFF7A3D)
val OrangeGradStart  = Color(0xFFFF9152)
val OrangeGradEnd    = Color(0xFFFF5E3A)
val OrangeDark       = Color(0xFFFF8C42)

val BackgroundLight  = Color(0xFFFFF8F3)
val SurfaceLight     = Color(0xFFFFFFFF)
val BackgroundDark   = Color(0xFF1E1A17)
val SurfaceDark      = Color(0xFF2B2420)

val OnPrimaryLight   = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF1E1A17)
val OnBackgroundDark  = Color(0xFFFFF8F3)

// Nota: por padrão, o Material3 gera surfaceContainer/surfaceContainerLow/High/
// Highest e surfaceVariant a partir de uma paleta tonal própria (tons roxo/lilás
// da paleta baseline), mesmo quando `surface` é sobrescrito. Isso fazia com que
// Card() (que usa surfaceContainerLowest por padrão) ficasse com um tom
// acinzentado/lilás diferente do branco puro do SPEC. Por isso sobrescrevemos
// explicitamente todos os tons de superfície para o mesmo branco (claro) ou
// mesmo cinza-carvão de card (escuro), garantindo cards 100% consistentes em
// qualquer componente Material3 padrão, não só nos que passam `colors=` manual.
private val LightColorScheme = lightColorScheme(
    primary                 = OrangePrimary,
    onPrimary               = OnPrimaryLight,
    primaryContainer        = OrangeGradStart,
    background              = BackgroundLight,
    surface                 = SurfaceLight,
    surfaceVariant          = SurfaceLight,
    surfaceContainerLowest  = SurfaceLight,
    surfaceContainerLow     = SurfaceLight,
    surfaceContainer        = SurfaceLight,
    surfaceContainerHigh    = SurfaceLight,
    surfaceContainerHighest = SurfaceLight,
    onBackground            = OnBackgroundLight,
    onSurface               = OnBackgroundLight,
    onSurfaceVariant        = OnBackgroundLight,
    secondary               = OrangeGradEnd,
    error                   = Color(0xFFB3261E),
)

private val DarkColorScheme = darkColorScheme(
    primary                 = OrangeDark,
    onPrimary               = OnPrimaryLight,
    primaryContainer        = Color(0xFF5C3A1E),
    background              = BackgroundDark,
    surface                 = SurfaceDark,
    surfaceVariant          = SurfaceDark,
    surfaceContainerLowest  = SurfaceDark,
    surfaceContainerLow     = SurfaceDark,
    surfaceContainer        = SurfaceDark,
    surfaceContainerHigh    = SurfaceDark,
    surfaceContainerHighest = SurfaceDark,
    onBackground            = OnBackgroundDark,
    onSurface               = OnBackgroundDark,
    onSurfaceVariant        = OnBackgroundDark,
    secondary               = OrangeGradEnd,
    error                   = Color(0xFFCF6679),
)

@Composable
fun PetCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(LocalPetCareSpacing provides PetCareSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = PetCareTypography,
            shapes      = PetCareShapes,
            content     = content,
        )
    }
}
