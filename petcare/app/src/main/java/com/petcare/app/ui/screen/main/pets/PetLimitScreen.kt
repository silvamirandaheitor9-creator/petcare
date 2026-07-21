package com.petcare.app.ui.screen.main.pets

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.PET_LIMIT_BONUS
import com.petcare.app.ui.viewmodel.PET_LIMIT_FREE

// ID de teste oficial do Google para rewarded ads
private const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

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
            REWARDED_TEST_AD_UNIT_ID,
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

            // ── Cabeçalho gradiente com ícone animado ─────────────────────────
            LimitHeader()

            Spacer(Modifier.height(24.dp))

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {

                // ── Título ────────────────────────────────────────────────────
                Text(
                    text       = "Limite de pets atingido!",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    textAlign  = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                // ── Descrição ─────────────────────────────────────────────────
                Text(
                    text = "Você atingiu o limite de $PET_LIMIT_FREE pets do plano gratuito. " +
                           "Assista a um anúncio curto e ganhe +$PET_LIMIT_BONUS vagas extras — " +
                           "de graça, sem precisar assinar nada!",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(Modifier.height(24.dp))

                // ── Contador visual de vagas ──────────────────────────────────
                SlotIndicator(
                    used  = petCount,
                    total = petLimit,
                    bonus = PET_LIMIT_BONUS,
                )

                Spacer(Modifier.height(28.dp))

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

                Spacer(Modifier.height(6.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text  = "Agora não",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
                    )
                }
            }
        }
    }
}

// ─── Cabeçalho com gradiente laranja e ícone animado ─────────────────────────

@Composable
private fun LimitHeader() {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // Animação de entrada: escala spring
    val scale by animateFloatAsState(
        targetValue   = if (entered) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "header_icon_scale",
    )

    // Rotação contínua suave no ícone de cadeado
    val rotation = rememberInfiniteTransition(label = "lock_rotation")
    val wobble by rotation.animateFloat(
        initialValue  = -6f,
        targetValue   = 6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "lock_wobble",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Círculo decorativo grande atrás
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Emoji de cadeado com animações
            Text(
                text     = "🔒",
                fontSize = 56.sp,
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer { rotationZ = wobble * scale },
            )
        }
    }
}

// ─── Indicador visual de vagas usadas/disponíveis ────────────────────────────

@Composable
private fun SlotIndicator(used: Int, total: Int, bonus: Int) {
    val newTotal = total + bonus

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Linha: "X de Y vagas usadas"
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text       = "$used",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = OrangePrimary,
            )
            Text(
                text  = "de $total vagas usadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
            )
        }

        // Barra de progresso personalizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            // Porção usada
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (used.toFloat() / newTotal).coerceIn(0f, 1f))
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
                    ),
            )
            // Linha divisória mostrando onde estão as vagas bonus
            if (bonus > 0 && total < newTotal) {
                val dividerFraction = total.toFloat() / newTotal
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = dividerFraction)
                        .align(Alignment.CenterEnd),
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(12.dp)
                            .align(Alignment.CenterEnd)
                            .background(Color.White.copy(alpha = 0.7f)),
                    )
                }
            }
        }

        // Legenda
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text  = "Plano gratuito ($total vagas)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.50f)),
                )
                Text(
                    text  = "+$bonus grátis",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangePrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─── Botão com animação de pulso ──────────────────────────────────────────────

@Composable
private fun PulsatingButton(
    isLoading: Boolean,
    loadFailed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
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

    // Halo pulsante atrás do botão
    Box(
        modifier            = Modifier.fillMaxWidth(),
        contentAlignment    = Alignment.Center,
    ) {
        if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(pulseScale * 1.04f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OrangePrimary.copy(alpha = 0.20f)),
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
                        modifier    = Modifier.size(22.dp),
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
                        text       = "😔  Anúncio indisponível",
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.bodyLarge,
                    )
                }
                else -> {
                    Text(
                        text       = "▶  Assistir anúncio e ganhar vagas",
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
