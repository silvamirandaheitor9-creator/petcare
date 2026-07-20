package com.petcare.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DampingRatioLowBouncy
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.spacing
import kotlinx.coroutines.launch

/**
 * Splash screen — redesign criativo (seção 4 do SPEC).
 *
 * Visual:
 *  - Fundo com gradiente diagonal OrangeGradStart → OrangeGradEnd
 *  - Patas decorativas espalhadas pelo fundo (baixa opacidade)
 *  - Halo suave atrás do mascote para suavizar o fundo da imagem
 *  - Mascote com quique de entrada + respiração suave depois de pousar
 *  - Título "Pet" (light) + "Care" (extrabold) para dar personalidade
 *  - Tagline com fade + slide
 *  - Indicador de carregamento: 3 patas pulsando em cascata (não bolinhas)
 *
 * Navegação só ocorre quando animação terminou E DataStore respondeu.
 */
@Composable
fun SplashScreen(
    isReady: Boolean,
    onNavigate: () -> Unit,
) {
    // ── Gradiente de fundo ────────────────────────────────────────────────────
    val backgroundBrush = remember {
        Brush.linearGradient(
            colors = listOf(OrangeGradStart, OrangeGradEnd),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    // ── Animatables de entrada ────────────────────────────────────────────────
    val mascotScale    = remember { Animatable(0f) }
    val titleAlpha     = remember { Animatable(0f) }
    val subtitleAlpha  = remember { Animatable(0f) }
    val pawsAlpha      = remember { Animatable(0f) }
    val titleOffsetY   = remember { Animatable(28f) }
    val subtitleOffsetY = remember { Animatable(18f) }

    var mascotLanded  by remember { mutableStateOf(false) }
    var animationDone by remember { mutableStateOf(false) }

    // ── Respiração do mascote (ativa só depois que pousou) ────────────────────
    val breathTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by breathTransition.animateFloat(
        initialValue = 1.00f,
        targetValue  = 1.038f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )

    // ── Patas de carregamento pulsando em cascata ─────────────────────────────
    val pawTransition = rememberInfiniteTransition(label = "paws")
    val paw1Scale by pawTransition.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "paw1",
    )
    val paw2Scale by pawTransition.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(550, delayMillis = 180), RepeatMode.Reverse),
        label = "paw2",
    )
    val paw3Scale by pawTransition.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(550, delayMillis = 360), RepeatMode.Reverse),
        label = "paw3",
    )

    // ── Sequência de entrada ──────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // 1. Mascote com overshoot (spring)
        mascotScale.animateTo(
            targetValue   = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness    = Spring.StiffnessHigh,
            ),
        )
        mascotLanded = true

        // 2. Título + tagline + patas em paralelo
        launch { titleOffsetY.animateTo(0f,  tween(420)) }
        launch { titleAlpha.animateTo(1f,    tween(420)) }
        launch { subtitleOffsetY.animateTo(0f, tween(380)) }
        launch { subtitleAlpha.animateTo(1f,  tween(380)) }
        pawsAlpha.animateTo(1f, tween(380))

        // Tempo mínimo de exibição (~1,1s após animação = ~2,3s total)
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
            .background(backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {

        // Patas decorativas no fundo
        DecorativePaws()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Mascote com halo suave ────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer {
                    val finalScale = mascotScale.value *
                        if (mascotLanded) breathScale else 1f
                    scaleX = finalScale
                    scaleY = finalScale
                },
            ) {
                // Halo — suaviza o fundo escuro da imagem png
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .alpha(0.18f)
                        .background(Color.White, CircleShape),
                )
                // Mascote
                Image(
                    painter            = painterResource(R.drawable.mascote_splash),
                    contentDescription = null,
                    modifier           = Modifier.size(220.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Título "PetCare" com tipografia com personalidade ─────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Light,
                        color      = Color.White.copy(alpha = 0.92f),
                    )) { append("Pet") }
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                    )) { append("Care") }
                },
                fontFamily    = MaterialTheme.typography.displaySmall.fontFamily,
                fontSize      = 44.sp,
                letterSpacing = 2.sp,
                modifier      = Modifier.graphicsLayer {
                    alpha        = titleAlpha.value
                    translationY = titleOffsetY.value
                },
            )

            Spacer(Modifier.height(6.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text      = "Cuidando dos seus pets com carinho",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.82f),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .padding(horizontal = MaterialTheme.spacing.lg)
                    .graphicsLayer {
                        alpha        = subtitleAlpha.value
                        translationY = subtitleOffsetY.value
                    },
            )

            Spacer(Modifier.height(36.dp))

            // ── Patas de carregamento (substituem os bolinhas genéricos) ──────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = pawsAlpha.value },
            ) {
                listOf(paw1Scale, paw2Scale, paw3Scale).forEach { scale ->
                    Icon(
                        imageVector        = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.78f),
                        modifier           = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                }
            }
        }
    }
}

// ── Patas decorativas espalhadas no fundo ─────────────────────────────────────
@Composable
private fun DecorativePaws() {
    val density = LocalDensity.current

    // (xFrac, yFrac, rotationDeg, sizeDp, alpha)
    data class PawSpec(
        val xFrac: Float,
        val yFrac: Float,
        val rotation: Float,
        val size: Float,
        val alpha: Float,
    )

    val specs = remember {
        listOf(
            PawSpec(0.06f, 0.06f, -28f, 30f, 0.13f),
            PawSpec(0.80f, 0.09f,  18f, 38f, 0.10f),
            PawSpec(0.03f, 0.40f,  42f, 24f, 0.09f),
            PawSpec(0.87f, 0.35f, -18f, 32f, 0.11f),
            PawSpec(0.10f, 0.78f,  20f, 28f, 0.10f),
            PawSpec(0.82f, 0.75f, -38f, 34f, 0.09f),
            PawSpec(0.46f, 0.03f,   8f, 22f, 0.08f),
            PawSpec(0.62f, 0.91f, -12f, 36f, 0.10f),
            PawSpec(0.30f, 0.88f,  30f, 26f, 0.08f),
            PawSpec(0.70f, 0.18f, -22f, 20f, 0.09f),
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx  = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        specs.forEach { spec ->
            val xDp = with(density) { (widthPx  * spec.xFrac).toDp() }
            val yDp = with(density) { (heightPx * spec.yFrac).toDp() }

            Icon(
                imageVector        = Icons.Rounded.Pets,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier
                    .absoluteOffset(xDp, yDp)
                    .size(spec.size.dp)
                    .rotate(spec.rotation)
                    .alpha(spec.alpha),
            )
        }
    }
}
