package com.petcare.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.theme.spacing
import kotlinx.coroutines.launch

/**
 * Splash screen (seção 4 do SPEC).
 *
 * Layout limpo e temático: fundo laranja sólido, mascote grande centralizado,
 * título com personalidade tipográfica e três pontos de carregamento animados
 * (pulsando em cascata) abaixo da tagline.
 *
 * A navegação (`onNavigate`) só é chamada quando:
 *   1. `animationDone` — animação de entrada concluída
 *   2. `isReady` — DataStore respondeu (já viu onboarding ou não)
 */
@Composable
fun SplashScreen(
    isReady: Boolean,
    onNavigate: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current

    // ── Animatables de entrada ────────────────────────────────────────────────
    val mascotScale   = remember { Animatable(0f) }
    val titleAlpha    = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val dotsAlpha     = remember { Animatable(0f) }
    val titleOffsetPx    = remember { with(density) { 24.dp.toPx() } }
    val subtitleOffsetPx = remember { with(density) { 16.dp.toPx() } }
    val titleOffset    = remember { Animatable(titleOffsetPx) }
    val subtitleOffset = remember { Animatable(subtitleOffsetPx) }

    var animationDone by remember { mutableStateOf(false) }

    // ── Pontos de carregamento pulsando em cascata ────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "dot1",
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 160), RepeatMode.Reverse),
        label = "dot2",
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 320), RepeatMode.Reverse),
        label = "dot3",
    )

    // ── Sequência de entrada ──────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // 1) Mascote com overshoot
        mascotScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness    = Spring.StiffnessHigh,
            ),
        )
        // 2) Título + subtítulo + pontos em paralelo
        launch { titleOffset.animateTo(0f, tween(420)) }
        launch { titleAlpha.animateTo(1f, tween(420)) }
        launch { subtitleOffset.animateTo(0f, tween(380)) }
        launch { subtitleAlpha.animateTo(1f, tween(380)) }
        dotsAlpha.animateTo(1f, tween(380))

        // Tempo mínimo de exibição — garante que a splash não some rápido demais
        // (~1.3s animação + 1.1s aqui = ~2.4s total, confortável sem ser longo)
        kotlinx.coroutines.delay(1_100)

        animationDone = true
    }

    LaunchedEffect(animationDone, isReady) {
        if (animationDone && isReady) onNavigate()
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangePrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Mascote
            Image(
                painter = painterResource(id = R.drawable.mascote_splash),
                contentDescription = null,
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = mascotScale.value
                        scaleY = mascotScale.value
                    },
            )

            Spacer(Modifier.height(spacing.md))

            // Título
            Text(
                text = "PetCare",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha        = titleAlpha.value
                    translationY = titleOffset.value
                },
            )

            Spacer(Modifier.height(spacing.xs))

            // Tagline
            Text(
                text      = "Cuidando dos seus pets com carinho",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .padding(horizontal = spacing.lg)
                    .graphicsLayer {
                        alpha        = subtitleAlpha.value
                        translationY = subtitleOffset.value
                    },
            )

            Spacer(Modifier.height(spacing.lg))

            // Três pontos de carregamento pulsando em cascata
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = dotsAlpha.value },
            ) {
                listOf(dot1Scale, dot2Scale, dot3Scale).forEach { scale ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(Color.White.copy(alpha = 0.75f), CircleShape),
                    )
                }
            }
        }
    }
}
