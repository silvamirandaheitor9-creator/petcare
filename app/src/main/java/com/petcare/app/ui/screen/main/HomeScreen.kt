package com.petcare.app.ui.screen.main

import android.graphics.drawable.ColorDrawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.petcare.app.R
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.HomeViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─── Ponto de entrada da aba Início ─────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddPet: () -> Unit = {},
) {
    val pets by viewModel.pets.collectAsState()
    val petCount by viewModel.petCount.collectAsState()
    val nextVaccine by viewModel.nextVaccineReminder.collectAsState()
    val nextConsultation by viewModel.nextConsultationReminder.collectAsState()
    val dailyTip by viewModel.dailyTip.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // (1) Card de estatísticas
        item {
            StatsCard(
                petCount = petCount,
                nextVaccine = nextVaccine,
                nextConsultation = nextConsultation,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item { Spacer(Modifier.height(14.dp)) }

        // (2) Card de dica da Mel
        item {
            MelTipCard(
                tip = dailyTip,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item { Spacer(Modifier.height(20.dp)) }

        // (3) Lista de pets ou estado vazio
        if (pets.isEmpty()) {
            item {
                EmptyPetsSection(
                    onAddPet = onAddPet,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        } else {
            item {
                Text(
                    text = "Meus Pets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { Spacer(Modifier.height(10.dp)) }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(pets, key = { it.id }) { pet ->
                        PetHorizontalCard(pet = pet)
                    }
                }
            }
        }

        // (4) Espaçamento + banner AdMob
        item { Spacer(Modifier.height(28.dp)) }
        item {
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ─── Card de estatísticas (Total, Próxima vacina, Próxima consulta) ──────────

@Composable
private fun StatsCard(
    petCount: Int,
    nextVaccine: Reminder?,
    nextConsultation: Reminder?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Total de pets
            StatItem(
                label = "Total de pets",
                value = if (petCount == 0) "--" else "$petCount",
                modifier = Modifier.weight(1f),
            )

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 1.dp,
            )

            // Próxima vacina
            StatItem(
                label = "Próxima vacina",
                value = nextVaccine?.dateTimeMillis?.toShortDate() ?: "--",
                modifier = Modifier.weight(1f),
            )

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 1.dp,
            )

            // Próxima consulta
            StatItem(
                label = "Próxima consulta",
                value = nextConsultation?.dateTimeMillis?.toShortDate() ?: "--",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (value == "--")
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            else
                OrangePrimary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

// ─── Card de dica da Mel ─────────────────────────────────────────────────────

@Composable
private fun MelTipCard(
    tip: MelTip,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = OrangePrimary.copy(alpha = 0.08f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar da Mel (pequeno)
            Image(
                painter = painterResource(R.drawable.mel_avatar_pequeno),
                contentDescription = "Mel",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Dica de hoje 🐾",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                )
                Text(
                    text = tip.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }
    }
}

// ─── Card horizontal de pet (na LazyRow) ─────────────────────────────────────

@Composable
private fun PetHorizontalCard(pet: Pet) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Foto circular ou placeholder
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(
                        if (pet.photoPath.isNotEmpty()) File(pet.photoPath)
                        else null
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.avatar_pet_padrao),
                error = painterResource(R.drawable.avatar_pet_padrao),
                placeholder = painterResource(R.drawable.avatar_pet_padrao),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
            )

            Text(
                text = pet.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = petAgeLabel(pet),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── Estado vazio: nenhum pet cadastrado ─────────────────────────────────────

@Composable
private fun EmptyPetsSection(
    onAddPet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animação de pulso sutil no botão para chamar atenção
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.055f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.vazio_meuspets),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.68f),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Seus pets estão esperando por você!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Adicione o primeiro pet e comece a cuidar com muito carinho.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        // Botão pill com pulso sutil
        Button(
            onClick = onAddPet,
            modifier = Modifier
                .scale(pulseScale)
                .fillMaxWidth(0.80f)
                .height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Adicionar meu primeiro pet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Banner AdMob ─────────────────────────────────────────────────────────────

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

// ─── Helpers privados ─────────────────────────────────────────────────────────

/** Formata milissegundos como "dd/MM" em pt-BR. */
private fun Long.toShortDate(): String {
    val sdf = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
    return sdf.format(Date(this))
}

/**
 * Retorna o rótulo de "idade" do pet para exibir no card:
 * - Idade calculada a partir de birthDate (formato ISO-8601 "yyyy-MM-dd")
 * - Fallback: approximateAge
 * - Fallback final: nome da espécie
 */
private fun petAgeLabel(pet: Pet): String {
    if (pet.birthDate.isNotBlank()) {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val bd = sdf.parse(pet.birthDate) ?: return pet.approximateAge.ifBlank { pet.species }
            val now = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { time = bd }
            val totalMonths =
                (now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12 +
                        now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
            when {
                totalMonths < 1  -> "< 1 mês"
                totalMonths < 12 -> "$totalMonths ${if (totalMonths == 1) "mês" else "meses"}"
                else             -> {
                    val years = totalMonths / 12
                    "$years ${if (years == 1) "ano" else "anos"}"
                }
            }
        } catch (e: Exception) {
            pet.approximateAge.ifBlank { pet.species }
        }
    }
    return pet.approximateAge.ifBlank { pet.species }
}
