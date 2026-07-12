package com.petcare.app.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.petcare.app.R
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.PetsViewModel
import kotlinx.coroutines.delay
import java.io.File

// ─── Ponto de entrada da aba Meus Pets (SPEC §8) ─────────────────────────────

@Composable
fun PetsScreen(
    viewModel: PetsViewModel = hiltViewModel(),
    onPetClick: (Long) -> Unit = {},
) {
    val pets by viewModel.pets.collectAsState()

    if (pets.isEmpty()) {
        EmptyPetsGridState()
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(pets, key = { _, pet -> pet.id }) { index, pet ->
                StaggeredPetCard(pet = pet, index = index, onPetClick = onPetClick)
            }

            // Banner AdMob — item de largura total ao final da grade
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(Modifier.height(4.dp))
                    BannerAdView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

// ─── Card com animação de entrada escalonada (SPEC 8.7) ──────────────────────

@Composable
private fun StaggeredPetCard(pet: Pet, index: Int, onPetClick: (Long) -> Unit = {}) {
    var visible by remember(pet.id) { mutableStateOf(false) }

    LaunchedEffect(pet.id) {
        // Atraso proporcional ao índice na grade, limitado a ~400ms.
        delay((index * 60L).coerceAtMost(400L))
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(280)) +
            slideInVertically(
                animationSpec = tween(280),
                initialOffsetY = { it / 4 },
            ),
    ) {
        PetGridCard(pet = pet, onPetClick = onPetClick)
    }
}

// ─── Card individual de pet na grade 2 colunas (SPEC 8.3–8.4, 8.8) ───────────

@Composable
private fun PetGridCard(pet: Pet, onPetClick: (Long) -> Unit = {}) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Compressão sutil ao toque (SPEC 8.8) — clique navega para o detalhe do pet (seção 12).
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "pet_card_press_${pet.id}",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onPetClick(pet.id) },
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (pet.photoPath.isNotEmpty()) File(pet.photoPath) else null)
                    .crossfade(true)
                    .build(),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.avatar_pet_padrao),
                error = painterResource(R.drawable.avatar_pet_padrao),
                placeholder = painterResource(R.drawable.avatar_pet_padrao),
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
            )

            Text(
                text = pet.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            val speciesBreed = if (pet.breed.isNotBlank()) "${pet.species} · ${pet.breed}" else pet.species
            Text(
                text = speciesBreed,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            SexCastrationBadge(pet = pet)
        }
    }
}

// ─── Selo de sexo/castração ───────────────────────────────────────────────────

@Composable
private fun SexCastrationBadge(pet: Pet) {
    if (pet.sex.isBlank() && !pet.isCastrated) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (pet.sex.trim().lowercase()) {
            "macho" -> Icon(
                imageVector = Icons.Rounded.Male,
                contentDescription = "Macho",
                tint = OrangePrimary,
                modifier = Modifier.size(16.dp),
            )
            "fêmea", "femea" -> Icon(
                imageVector = Icons.Rounded.Female,
                contentDescription = "Fêmea",
                tint = OrangePrimary,
                modifier = Modifier.size(16.dp),
            )
        }
        if (pet.isCastrated) {
            Icon(
                imageVector = Icons.Rounded.ContentCut,
                contentDescription = "Castrado(a)",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// ─── Estado vazio: nenhum pet cadastrado (SPEC 8.5) ──────────────────────────
// Totalmente centralizado na tela (evita o bug documentado de imagem no
// canto); sem botão de CTA duplicado, já que a aba tem seu próprio FAB "+".

@Composable
private fun EmptyPetsGridState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.vazio_meuspets),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.6f),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Nenhum pet por aqui ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Toque no + para cadastrar seu primeiro pet com muito carinho.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Banner AdMob (SPEC 8.9) ──────────────────────────────────────────────────
// Cópia privada por tela, seguindo o padrão já usado em HomeScreen.kt.

@Composable
private fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test banner ID
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier,
    )
}
