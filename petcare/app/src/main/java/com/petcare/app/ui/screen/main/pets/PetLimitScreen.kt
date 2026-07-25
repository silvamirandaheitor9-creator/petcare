package com.petcare.app.ui.screen.main.pets

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Star
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
// Simplificado: foco direto no CTA, sem animações excessivas

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
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Cabeçalho simples com imagem ──────────────────────────────────
            LimitHeader()

            Spacer(Modifier.height(24.dp))

            // ── Título ────────────────────────────────────────────────────────
            Text(
                text       = "Limite de pets atingido",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onSurface,
                textAlign  = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            // ── Descrição clara e concisa ─────────────────────────────────────
            Text(
                text = "Você tem $petCount de $petLimit pets. Assista um anúncio rápido para ganhar +$PET_LIMIT_BONUS vagas extras!",
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                textAlign  = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(28.dp))

            // ── Grade visual de vagas ─────────────────────────────────────────
            SlotGridSimple(
                used    = petCount,
                base    = petLimit,
                bonus   = PET_LIMIT_BONUS,
            )

            Spacer(Modifier.height(28.dp))

            // ── Botão principal ──────────────────────────────────────────────────
            Button(
                onClick = {
                    val activity = context as? Activity ?: return@Button
                    rewardedAd?.show(activity) { _ ->
                        onUnlocked()
                        onDismiss()
                    }
                },
                enabled  = !isLoading && !loadFailed && rewardedAd != null,
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
                            text       = "Assistir anúncio e desbloquear",
                            fontWeight = FontWeight.ExtraBold,
                            style      = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Botão secundário ─────────────────────────────────────────────────
            TextButton(onClick = onDismiss) {
                Text(
                    text  = "Agora não",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
                )
            }
        }
    }
}

// ─── Cabeçalho simples com imagem ────────────────────────────────────────────

@Composable
private fun LimitHeader() {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // Animação de entrada suave: apenas escala, sem balanço
    val scale by animateFloatAsState(
        targetValue   = if (entered) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "header_image_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
            )
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        // Imagem principal do mascote (simples, sem decorações)
        androidx.compose.foundation.Image(
            painter           = painterResource(R.drawable.feedback_desbloquear),
            contentDescription= "Limite de pets",
            modifier          = Modifier
                .size(80.dp)
                .scale(scale),
            contentScale      = ContentScale.Fit,
        )
    }
}

// ─── Grade visual simplificada ───────────────────────────────────────────────

@Composable
private fun SlotGridSimple(used: Int, base: Int, bonus: Int) {
    val total   = base + bonus
    val columns = 5

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Texto de contagem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text       = "$used",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = OrangePrimary,
            )
            Text(
                text  = "de $base vagas usadas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
            )
        }

        Spacer(Modifier.height(4.dp))

        // Grade de círculos
        val rows = (total + columns - 1) / columns
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < total) {
                        val state = when {
                            index < used  -> SlotState.USED
                            index < base  -> SlotState.FREE
                            else          -> SlotState.BONUS
                        }
                        SlotCircleSimple(state = state)
                    }
                }
            }
        }
    }
}

private enum class SlotState { USED, FREE, BONUS }

@Composable
private fun SlotCircleSimple(state: SlotState) {
    val size: Dp = 28.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                when (state) {
                    SlotState.USED  -> Modifier.background(
                        Brush.radialGradient(listOf(OrangeGradStart, OrangeGradEnd))
                    )
                    SlotState.FREE  -> Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), CircleShape)
                    SlotState.BONUS -> Modifier
                        .background(OrangePrimary.copy(alpha = 0.12f))
                        .border(1.5.dp, OrangePrimary.copy(alpha = 0.40f), CircleShape)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            SlotState.USED  -> Icon(
                Icons.Rounded.Pets,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(14.dp),
            )
            SlotState.BONUS -> Text(
                "+",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = OrangePrimary,
            )
            SlotState.FREE  -> {}
        }
    }
}
