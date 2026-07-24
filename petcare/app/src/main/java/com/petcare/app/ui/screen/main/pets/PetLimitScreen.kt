package com.petcare.app.ui.screen.main.pets

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.PET_LIMIT_BONUS
import com.petcare.app.ui.viewmodel.PET_LIMIT_FREE
import kotlinx.coroutines.delay

// ID de produção — Rewarded Ad Unit
private const val REWARDED_AD_UNIT_ID = "ca-app-pub-2930629233574738/9944805172"

// ─── Bottom sheet de limite de pets (SPEC §18.3-18.4 + §16.6) ────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetLimitSheet(
    petCount: Int,
    petLimit: Int,
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context    = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Carregamento do rewarded ad ───────────────────────────────────────────
    var rewardedAd   by remember { mutableStateOf<RewardedAd?>(null) }
    var isLoading    by remember { mutableStateOf(true) }
    var loadFailed   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading  = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading  = false
                    loadFailed = true
                }
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Cabeçalho com imagem e gradiente ─────────────────────────────
            LimitHeader()

            Spacer(Modifier.height(20.dp))

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // ── Badge "+5 vagas grátis" ───────────────────────────────────
                BonusBadge()

                Spacer(Modifier.height(12.dp))

                // ── Título ────────────────────────────────────────────────────
                Text(
                    text       = "Você chegou ao limite!",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    textAlign  = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                // ── Descrição ─────────────────────────────────────────────────
                Text(
                    text = "O plano gratuito inclui $PET_LIMIT_FREE pets. " +
                           "Assista a um anúncio curto e ganhe $PET_LIMIT_BONUS vagas extras — sem assinar nada.",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                    textAlign  = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(Modifier.height(22.dp))

                // ── Indicador de vagas individual ─────────────────────────────
                SlotGrid(
                    used    = petCount,
                    base    = petLimit,
                    bonus   = PET_LIMIT_BONUS,
                )

                Spacer(Modifier.height(22.dp))

                // ── Como funciona: 3 passos ───────────────────────────────────
                HowItWorksRow()

                Spacer(Modifier.height(22.dp))

                // ── Botão principal com pulso ─────────────────────────────────
                PulsatingButton(
                    isLoading  = isLoading,
                    loadFailed = loadFailed,
                    enabled    = !isLoading && !loadFailed && rewardedAd != null,
                    onClick    = {
                        val activity = context as? Activity ?: return@PulsatingButton
                        rewardedAd?.show(activity) { _ ->
                            onUnlocked()
                            onDismiss()
                        }
                    },
                )

                Spacer(Modifier.height(4.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text  = "Agora não",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                }
            }
        }
    }
}

// ─── Cabeçalho com imagem feedback_desbloquear e gradiente ───────────────────

@Composable
private fun LimitHeader() {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // Animação de entrada: escala spring na imagem
    val scale by animateFloatAsState(
        targetValue   = if (entered) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "header_image_scale",
    )

    // Balanço contínuo suave
    val rotation = rememberInfiniteTransition(label = "img_wobble")
    val wobble by rotation.animateFloat(
        initialValue  = -4f,
        targetValue   = 4f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "img_wobble_val",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Círculos decorativos concêntricos
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        )

        // Imagem principal do mascote com animações
        androidx.compose.foundation.Image(
            painter           = painterResource(R.drawable.feedback_desbloquear),
            contentDescription= "Limite de pets",
            modifier          = Modifier
                .size(110.dp)
                .scale(scale)
                .graphicsLayer { rotationZ = wobble * scale },
            contentScale      = ContentScale.Fit,
        )
    }
}

// ─── Badge "+5 vagas grátis" com animação de entrada ─────────────────────────

@Composable
private fun BonusBadge() {
    val alpha   = remember { Animatable(0f) }
    val offsetY = remember { Animatable(-12f) }

    LaunchedEffect(Unit) {
        delay(200)
        alpha.animateTo(1f, tween(300))
        offsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .graphicsLayer { this.alpha = alpha.value; translationY = offsetY.value }
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
            )
            .padding(horizontal = 18.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text       = "+$PET_LIMIT_BONUS vagas grátis disponíveis",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
            )
        }
    }
}

// ─── Grade de vagas (círculos individuais) ────────────────────────────────────

@Composable
private fun SlotGrid(used: Int, base: Int, bonus: Int) {
    val total   = base + bonus
    val columns = 5

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Texto de contagem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text       = "$used",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = OrangePrimary,
            )
            Text(
                text  = "de $base vagas usadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }

        Spacer(Modifier.height(2.dp))

        // Grade de círculos - stagger reveal
        val rows = (total + columns - 1) / columns
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < total) {
                        SlotCircle(
                            state = when {
                                index < used  -> SlotState.USED
                                index < base  -> SlotState.FREE
                                else          -> SlotState.BONUS
                            },
                            animDelay = index * 30,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Legenda
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = OrangePrimary, label = "Ocupada")
            LegendDot(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), label = "Livre")
            LegendDot(
                color = OrangePrimary.copy(alpha = 0.35f),
                label = "+$bonus ao assistir",
                isDashed = true,
            )
        }
    }
}

private enum class SlotState { USED, FREE, BONUS }

@Composable
private fun SlotCircle(state: SlotState, animDelay: Int) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(animDelay.toLong())
        scale.animateTo(
            1f,
            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        )
    }

    val size: Dp = 32.dp

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale.value)
            .clip(CircleShape)
            .then(
                when (state) {
                    SlotState.USED  -> Modifier.background(
                        Brush.radialGradient(listOf(OrangeGradStart, OrangeGradEnd))
                    )
                    SlotState.FREE  -> Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), CircleShape)
                    SlotState.BONUS -> Modifier
                        .background(OrangePrimary.copy(alpha = 0.08f))
                        .border(1.5.dp, OrangePrimary.copy(alpha = 0.35f), CircleShape)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            SlotState.USED  -> Icon(
                Icons.Rounded.Pets,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(16.dp),
            )
            SlotState.BONUS -> Text(
                "+",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = OrangePrimary.copy(alpha = 0.7f),
            )
            SlotState.FREE  -> {}
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, isDashed: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isDashed) Modifier.border(1.dp, OrangePrimary.copy(alpha = 0.5f), CircleShape)
                    else Modifier
                ),
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

// ─── Seção "Como funciona": 3 passos ─────────────────────────────────────────

@Composable
private fun HowItWorksRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        HowItWorksStep(icon = Icons.Rounded.VideoLibrary, label = "Assiste\num anúncio")
        StepArrow()
        HowItWorksStep(icon = Icons.Rounded.Star, label = "Ganha\n+$PET_LIMIT_BONUS vagas")
        StepArrow()
        HowItWorksStep(icon = Icons.Rounded.CheckCircle, label = "Cadastra\nmais pets")
    }
}

@Composable
private fun HowItWorksStep(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OrangePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
        }
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
            textAlign = TextAlign.Center,
            lineHeight= 16.sp,
        )
    }
}

@Composable
private fun StepArrow() {
    Text(
        "→",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        modifier = Modifier.padding(top = 10.dp),
    )
}

// ─── Botão com animação de pulso ──────────────────────────────────────────────

@Composable
private fun PulsatingButton(
    isLoading  : Boolean,
    loadFailed : Boolean,
    enabled    : Boolean,
    onClick    : () -> Unit,
) {
    val pulse = rememberInfiniteTransition(label = "btn_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.03f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "btn_pulse_scale",
    )

    Box(
        modifier         = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Halo pulsante
        if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(pulseScale * 1.05f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OrangePrimary.copy(alpha = 0.18f)),
            )
        }

        Button(
            onClick  = onClick,
            enabled  = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor         = OrangePrimary,
                contentColor           = Color.White,
                disabledContainerColor = OrangePrimary.copy(alpha = 0.35f),
                disabledContentColor   = Color.White,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            when {
                isLoading  -> {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = "Carregando anúncio…",
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.bodyLarge,
                    )
                }
                loadFailed -> {
                    Text(
                        text       = "Anúncio indisponível",
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.bodyLarge,
                    )
                }
                else -> {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Assistir e ganhar vagas",
                        fontWeight = FontWeight.ExtraBold,
                        style      = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
