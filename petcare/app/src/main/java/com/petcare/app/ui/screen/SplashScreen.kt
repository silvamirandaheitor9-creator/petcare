package com.petcare.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petcare.app.R
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
 * Sequência animada:
 *  - Anéis pulsantes + patas decorativas: InfiniteTransition (loop automático)
 *  - Mascote: escala com spring (overshoot leve)
 *  - Título e tagline: fade + slide em paralelo após o mascote
 *
 * A navegação (`onNavigate`) só é chamada quando as DUAS condições abaixo
 * forem verdadeiras — o que demorar mais decide o momento, nunca um timer
 * cego desconectado do carregamento real:
 *   1. `animationDone`: a sequência de animação acima terminou de fato.
 *   2. `isReady`: o ViewModel confirma que o DataStore já respondeu.
 */
@Composable
fun SplashScreen(
    isReady: Boolean,
    onNavigate: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current

    // ── Animatables de entrada ────────────────────────────────────────────────
    val mascotScale  = remember { Animatable(0f) }
    val titleAlpha   = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val titleOffsetStartPx    = remember { with(density) { 24.dp.toPx() } }
    val subtitleOffsetStartPx = remember { with(density) { 16.dp.toPx() } }
    val titleOffset    = remember { Animatable(titleOffsetStartPx) }
    val subtitleOffset = remember { Animatable(subtitleOffsetStartPx) }

    var animationDone by remember { mutableStateOf(false) }

    // ── Anéis pulsantes (loop contínuo) ──────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "rings")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.18f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring1Scale",
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue  = 0.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring1Alpha",
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1.18f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring2Scale",
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue  = 0.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring2Alpha",
    )

    // ── Rotação das patas nos cantos ─────────────────────────────────────────
    val pawRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(32_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pawRotation",
    )

    // ── Sequência de entrada ──────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // 1) Mascote: escala + overshoot via spring
        mascotScale.animateTo(
            targetValue  = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness    = Spring.StiffnessHigh,
            ),
        )

        // 2+3) Título e tagline em paralelo
        val titleJob = launch {
            launch { titleOffset.animateTo(0f, tween(durationMillis = 420)) }
            titleAlpha.animateTo(1f, tween(durationMillis = 420))
        }
        val subtitleJob = launch {
            launch { subtitleOffset.animateTo(0f, tween(durationMillis = 380)) }
            subtitleAlpha.animateTo(1f, tween(durationMillis = 380))
        }
        titleJob.join()
        subtitleJob.join()

        animationDone = true
    }

    LaunchedEffect(animationDone, isReady) {
        if (animationDone && isReady) onNavigate()
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OrangeGradStart, OrangeGradEnd))),
        contentAlignment = Alignment.Center,
    ) {

        // ── Patas decorativas nos 4 cantos (rotação alternada) ────────────────
        val cornerPaws = listOf(
            Triple(Alignment.TopStart,     28f,  1f),   // canto superior esquerdo — horário
            Triple(Alignment.TopEnd,      -22f, -1f),   // canto superior direito  — anti-horário
            Triple(Alignment.BottomStart,  18f, -1f),   // canto inferior esquerdo — anti-horário
            Triple(Alignment.BottomEnd,   -38f,  1f),   // canto inferior direito  — horário
        )
        cornerPaws.forEach { (alignment, baseAngle, direction) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = alignment,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Pets,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer {
                            rotationZ = baseAngle + pawRotation * direction
                        },
                )
            }
        }

        // ── Conteúdo central ─────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.offset(y = (-20).dp),
        ) {

            // Anéis pulsantes atrás do mascote
            val ringBaseSize = 200.dp
            Box(
                contentAlignment = Alignment.Center,
            ) {
                // Anel externo
                Box(
                    modifier = Modifier
                        .size(ringBaseSize)
                        .graphicsLayer {
                            scaleX = ring2Scale
                            scaleY = ring2Scale
                        }
                        .drawBehind {
                            drawCircle(
                                color  = Color.White.copy(alpha = ring2Alpha),
                                style  = Stroke(width = 2.dp.toPx()),
                                radius = size.minDimension / 2f,
                            )
                        },
                )

                // Anel interno
                Box(
                    modifier = Modifier
                        .size(ringBaseSize * 0.76f)
                        .graphicsLayer {
                            scaleX = ring1Scale
                            scaleY = ring1Scale
                        }
                        .drawBehind {
                            drawCircle(
                                color  = Color.White.copy(alpha = ring1Alpha),
                                style  = Stroke(width = 2.5.dp.toPx()),
                                radius = size.minDimension / 2f,
                            )
                        },
                )

                // Mascote
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
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            // Título "PetCare"
            Text(
                text = "PetCare",
                style = MaterialTheme.typography.displaySmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight    = FontWeight.Bold,
                ),
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha        = titleAlpha.value
                    translationY = titleOffset.value
                },
            )

            // Divisor decorativo fino
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = subtitleAlpha.value }
                    .width(44.dp)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.52f)),
            )

            // Tagline
            Text(
                text      = "O cuidado que seu pet merece, sempre com você.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = Color.White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .padding(horizontal = spacing.lg)
                    .graphicsLayer {
                        alpha        = subtitleAlpha.value
                        translationY = subtitleOffset.value
                    },
            )
        }
    }
}
