package com.petcare.app.ui.screen.main.pets

import android.app.Activity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.PET_LIMIT_BONUS
import com.petcare.app.ui.viewmodel.PET_LIMIT_FREE

// ID de teste oficial do Google para rewarded ads
private const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

// ─── Bottom sheet de limite de pets (SPEC §18.3-18.4 + §16.6) ────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetLimitSheet(
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Carregamento do rewarded ad ───────────────────────────────────────────
    var rewardedAd   by remember { mutableStateOf<RewardedAd?>(null) }
    var isLoading    by remember { mutableStateOf(true) }
    var loadFailed   by remember { mutableStateOf(false) }
    var watchSuccess by remember { mutableStateOf(false) }

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
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Imagem feedback_desbloquear com efeito "caixa abrindo" (SPEC 16.6)
            UnlockAnimation()

            Spacer(Modifier.height(8.dp))

            Text(
                text       = "Limite de pets atingido!",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                textAlign  = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "O plano gratuito do PetCare permite cadastrar até " +
                       "$PET_LIMIT_FREE pets. Assista a um anúncio curto e " +
                       "ganhe mais $PET_LIMIT_BONUS vagas — de graça, sem assinar nada! 🎉",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            // ── Botão principal ───────────────────────────────────────────────
            Button(
                onClick = {
                    val activity = context as? Activity ?: return@Button
                    rewardedAd?.show(activity) { _ ->
                        // Recompensa concedida — libera as vagas extras
                        watchSuccess = true
                        onUnlocked()
                        onDismiss()
                    }
                },
                enabled  = !isLoading && !loadFailed && rewardedAd != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape  = RoundedCornerShape(14.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = androidx.compose.ui.graphics.Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text       = if (loadFailed) "Anúncio indisponível" else "▶  Assistir anúncio",
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text(
                    text  = "Agora não",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

// ─── Animação "caixa abrindo" para feedback_desbloquear.png (SPEC 16.6) ──────
//
// Efeito: a imagem entra com spring de escala (0 → 1) + rotação suave (−8° → 0°)
// que imita uma tampa de caixa se abrindo; depois oscila levemente para cima/baixo
// em loop para dar vida à ilustração enquanto o usuário lê o texto.

@Composable
private fun UnlockAnimation() {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // Entrada: escala spring + rotação se abrindo
    val entryScale by animateFloatAsState(
        targetValue   = if (entered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "unlock_entry_scale",
    )
    val entryRotation by animateFloatAsState(
        targetValue   = if (entered) 0f else -12f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "unlock_entry_rotation",
    )

    // Loop: flutua suavemente (oscilação Y contínua)
    val bounceTr = rememberInfiniteTransition(label = "unlock_bounce")
    val bounceY by bounceTr.animateFloat(
        initialValue  = 0f,
        targetValue   = -8f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "unlock_bounce_y",
    )

    Image(
        painter            = painterResource(R.drawable.feedback_desbloquear),
        contentDescription = "Desbloquear vagas extras",
        modifier           = Modifier
            .size(180.dp)
            .graphicsLayer {
                scaleX        = entryScale
                scaleY        = entryScale
                rotationZ     = entryRotation * entryScale
                translationY  = bounceY * entryScale
            },
    )
}
