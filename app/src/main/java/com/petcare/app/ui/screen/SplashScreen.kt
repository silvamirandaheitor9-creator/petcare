package com.petcare.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.petcare.app.R
import com.petcare.app.debug.StartupTimer
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.spacing
import kotlinx.coroutines.launch

/**
 * Splash screen (seção 4 do SPEC).
 *
 * Aparece imediatamente ao abrir o app — a tela branca antes dela já é
 * eliminada pelo tema `Theme.PetCare.Splash` (windowBackground laranja) no
 * AndroidManifest, então esta Composable já nasce sobre um fundo colorido,
 * nunca branco.
 *
 * Sequência animada: mascote (escala + leve quique/overshoot) → nome
 * "PetCare" (fade + slide) → frase de efeito (fade + slide), uma depois da
 * outra.
 *
 * A navegação (`onNavigate`) só é chamada quando as DUAS condições abaixo
 * forem verdadeiras — o que demorar mais decide o momento, nunca um timer
 * cego desconectado do carregamento real:
 *   1. `animationDone`: a sequência de animação acima terminou de fato
 *      (medido pelo fim real das `animateTo`, não por um `delay` arbitrário).
 *   2. `isReady`: o ViewModel confirma que o DataStore já respondeu se o
 *      onboarding foi concluído.
 */
@Composable
fun SplashScreen(
    isReady: Boolean,
    onNavigate: () -> Unit,
) {
    remember { StartupTimer.mark("SplashScreen: composition start") }

    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current

    val mascotScale = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val titleOffsetStartPx = remember { with(density) { 24.dp.toPx() } }
    val subtitleOffsetStartPx = remember { with(density) { 16.dp.toPx() } }
    val titleOffset = remember { Animatable(titleOffsetStartPx) }
    val subtitleOffset = remember { Animatable(subtitleOffsetStartPx) }

    var animationDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        StartupTimer.mark("LaunchedEffect(Unit): started (animation begins)")

        // 1) Mascote entra com escala + leve quique (overshoot via spring).
        // DampingRatioMediumBouncy + StiffnessLow ficava sutil demais — o
        // "quique" quase não era perceptível. LowBouncy (menos amortecimento)
        // combinado com StiffnessMedium (mais rígido/rápido) faz a escala
        // ultrapassar visivelmente 1f antes de assentar, sem ficar lento.
        mascotScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
        StartupTimer.mark("mascotScale animation done")

        // 2) Nome "PetCare" surge com fade + slide de baixo para cima.
        launch { titleOffset.animateTo(0f, tween(durationMillis = 420)) }
        titleAlpha.animateTo(1f, tween(durationMillis = 420))
        StartupTimer.mark("title animation done")

        // 3) Frase final aparece por último, mesmo estilo de entrada.
        launch { subtitleOffset.animateTo(0f, tween(durationMillis = 380)) }
        subtitleAlpha.animateTo(1f, tween(durationMillis = 380))
        StartupTimer.mark("subtitle animation done")

        // A partir daqui a animação mínima terminou de verdade.
        animationDone = true
        StartupTimer.mark("animationDone = true")
    }

    LaunchedEffect(animationDone, isReady) {
        if (animationDone && isReady) {
            StartupTimer.mark("onNavigate() called (animationDone && isReady)")
            onNavigate()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OrangeGradStart, OrangeGradEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Image(
                painter = painterResource(id = R.drawable.mascote_splash),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        scaleX = mascotScale.value
                        scaleY = mascotScale.value
                    },
            )

            Text(
                text = "PetCare",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                },
            )

            Text(
                text = "Cuidando dos seus pets com carinho",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(horizontal = spacing.md)
                    .graphicsLayer {
                        alpha = subtitleAlpha.value
                        translationY = subtitleOffset.value
                    },
            )
        }

        // DEBUG TEMPORÁRIO: overlay com timestamps de startup para
        // diagnosticar o atraso na splash. Remover após a causa raiz ser
        // confirmada e corrigida.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(spacing.sm),
        ) {
            StartupTimer.marks.forEach { (label, t) ->
                Text(
                    text = "+${t}ms  $label",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}
