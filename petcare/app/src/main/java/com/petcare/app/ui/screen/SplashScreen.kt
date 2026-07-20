package com.petcare.app.ui.screen

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isReady: Boolean,
    onNavigate: () -> Unit,
) {
    // ── Animatables de entrada ───────────────────────────────────────
    val mascotOffsetY = remember { Animatable(-420f) }
    val mascotScale   = remember { Animatable(0.78f) }
    val titleAlpha    = remember { Animatable(0f) }
    val titleScale    = remember { Animatable(0.82f) }
    val titleOffsetY  = remember { Animatable(18f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val pawsAlpha     = remember { Animatable(0f) }

    var animationDone by remember { mutableStateOf(false) }

    // ── Pulso do halo atrás do mascote ──────────────────────────────
    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by glowTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.09f,
        animationSpec = infiniteRepeatable(
            tween(1900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "glowScale",
    )

    // ── Deriva suave dos ícones de fundo ────────────────────────────
    val driftTransition = rememberInfiniteTransition(label = "drift")
    val drift by driftTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 16f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "drift",
    )

    // ── Patas de carregamento (substituem os três pontos) ───────────
    val pawTransition = rememberInfiniteTransition(label = "paws")
    val paw1S by pawTransition.animateFloat(
        0.50f, 1f,
        infiniteRepeatable(tween(480), RepeatMode.Reverse),
        "p1",
    )
    val paw2S by pawTransition.animateFloat(
        0.50f, 1f,
        infiniteRepeatable(tween(480, delayMillis = 160), RepeatMode.Reverse),
        "p2",
    )
    val paw3S by pawTransition.animateFloat(
        0.50f, 1f,
        infiniteRepeatable(tween(480, delayMillis = 320), RepeatMode.Reverse),
        "p3",
    )

    // ── Sequência de entrada ─────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Mascote cai do alto com quique
        launch {
            mascotOffsetY.animateTo(
                0f,
                spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
            )
        }
        mascotScale.animateTo(1f, tween(520))

        // Título surge com zoom + slide
        launch { titleAlpha.animateTo(1f, tween(360)) }
        launch { titleScale.animateTo(1f, tween(360)) }
        launch { titleOffsetY.animateTo(0f, tween(360)) }

        kotlinx.coroutines.delay(160)
        launch { subtitleAlpha.animateTo(1f, tween(340)) }

        kotlinx.coroutines.delay(210)
        pawsAlpha.animateTo(1f, tween(280))

        kotlinx.coroutines.delay(1_100)
        animationDone = true
    }

    LaunchedEffect(animationDone, isReady) {
        if (animationDone && isReady) onNavigate()
    }

    // ── Layout ───────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(OrangeGradStart, OrangeGradEnd, Color(0xFFEE4A1C)),
                ),
            ),
    ) {
        // ── Ícones de espécie flutuando no fundo (decorativos) ───────
        val bgAlpha = 0.11f
        val bgSize  = 52.dp

        // Canto superior esquerdo — cachorro
        Image(
            painter            = painterResource(R.drawable.icone_especie_cachorro),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 58.dp)
                .size(bgSize)
                .graphicsLayer {
                    alpha        = bgAlpha
                    translationY = drift * 0.7f
                    rotationZ    = -18f
                },
        )
        // Canto superior direito — gato
        Image(
            painter            = painterResource(R.drawable.icone_especie_gato),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 30.dp, top = 96.dp)
                .size(bgSize)
                .graphicsLayer {
                    alpha        = bgAlpha
                    translationY = -drift * 0.5f
                    rotationZ    = 22f
                },
        )
        // Centro-esquerda — pássaro
        Image(
            painter            = painterResource(R.drawable.icone_especie_passaro),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 14.dp)
                .offset(y = (-72).dp)
                .size(bgSize - 8.dp)
                .graphicsLayer {
                    alpha        = bgAlpha
                    translationY = drift
                    rotationZ    = 14f
                },
        )
        // Centro-direita — roedor
        Image(
            painter            = painterResource(R.drawable.icone_especie_roedor),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .offset(y = 56.dp)
                .size(bgSize - 8.dp)
                .graphicsLayer {
                    alpha        = bgAlpha
                    translationY = -drift * 0.6f
                    rotationZ    = -16f
                },
        )
        // Inferior esquerdo — peixe
        Image(
            painter            = painterResource(R.drawable.icone_especie_peixe),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 38.dp, bottom = 128.dp)
                .size(bgSize - 4.dp)
                .graphicsLayer {
                    alpha        = bgAlpha
                    translationY = -drift * 0.8f
                    rotationZ    = 20f
                },
        )
        // Inferior direito — réptil
        Image(
            painter            = painterResource(R.drawable.icone_especie_reptil),
            contentDescription = null,
            modifier           = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 34.dp, bottom = 168.dp)
                .size(bgSize - 4.dp)
                .graphicsLayer {
                    alpha        = bgAlpha
                    translationY = drift * 0.9f
                    rotationZ    = -26f
                },
        )

        // ── Conteúdo central ─────────────────────────────────────────
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Halo pulsante + mascote sobrepostos
            Box(contentAlignment = Alignment.Center) {
                // Halo externo (maior, mais transparente)
                Box(
                    modifier = Modifier
                        .size(228.dp)
                        .graphicsLayer { scaleX = glowScale * 1.06f; scaleY = glowScale * 1.06f }
                        .background(Color.White.copy(alpha = 0.05f), CircleShape),
                )
                // Halo interno (mais definido)
                Box(
                    modifier = Modifier
                        .size(196.dp)
                        .graphicsLayer { scaleX = glowScale; scaleY = glowScale }
                        .background(Color.White.copy(alpha = 0.11f), CircleShape),
                )
                // Mascote cai de cima
                Image(
                    painter            = painterResource(R.drawable.mascote_splash),
                    contentDescription = null,
                    modifier           = Modifier
                        .size(172.dp)
                        .graphicsLayer {
                            translationY = mascotOffsetY.value
                            scaleX       = mascotScale.value
                            scaleY       = mascotScale.value
                        },
                )
            }

            Spacer(Modifier.height(32.dp))

            // Nome do app com zoom + slide
            Text(
                text          = "PetCare",
                fontSize      = 40.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color.White,
                letterSpacing = 1.8.sp,
                modifier      = Modifier.graphicsLayer {
                    alpha        = titleAlpha.value
                    scaleX       = titleScale.value
                    scaleY       = titleScale.value
                    translationY = titleOffsetY.value
                },
            )

            Spacer(Modifier.height(8.dp))

            // Tagline com patinhas decorativas nas pontas
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier
                    .padding(horizontal = 40.dp)
                    .graphicsLayer { alpha = subtitleAlpha.value },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Pets,
                    contentDescription = null,
                    tint     = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(11.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text      = "Cuidando dos seus pets com carinho",
                    fontSize  = 13.sp,
                    color     = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.width(7.dp))
                Icon(
                    imageVector = Icons.Rounded.Pets,
                    contentDescription = null,
                    tint     = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(11.dp),
                )
            }

            Spacer(Modifier.height(56.dp))

            // Três patinhas pulsando em cascata (loading)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.graphicsLayer { alpha = pawsAlpha.value },
            ) {
                listOf(paw1S, paw2S, paw3S).forEach { scale ->
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint     = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                    )
                }
            }
        }
    }
}
