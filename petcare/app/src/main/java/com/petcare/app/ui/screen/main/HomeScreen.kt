package com.petcare.app.ui.screen.main


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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import androidx.compose.ui.platform.LocalContext
import com.petcare.app.R
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.HomeViewModel
import com.petcare.app.util.PetCareTips
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
    val pets             by viewModel.pets.collectAsState()
    val petCount         by viewModel.petCount.collectAsState()
    val nextVaccine      by viewModel.nextVaccineReminder.collectAsState()
    val nextConsultation by viewModel.nextConsultationReminder.collectAsState()
    val tipSpecies       = pets.firstOrNull()?.species

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // (1) Card de estatísticas
        item(key = "stats_card") {
            StatsCard(
                petCount         = petCount,
                nextVaccine      = nextVaccine,
                nextConsultation = nextConsultation,
                modifier         = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "stats_spacer") { Spacer(Modifier.height(16.dp)) }

        // (2) Card de dica do dia
        item(key = "tip_card") {
            TipCard(
                species  = tipSpecies,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "tip_spacer") { Spacer(Modifier.height(24.dp)) }

        // (3) Lista de pets ou estado vazio
        if (pets.isEmpty()) {
            item(key = "empty_pets") {
                EmptyPetsSection(
                    onAddPet = onAddPet,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        } else {
            item(key = "pets_header") {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text       = "Seus pets",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
            item(key = "pets_header_spacer") { Spacer(Modifier.height(12.dp)) }
            item(key = "pets_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                ) {
                    items(pets, key = { it.id }) { pet ->
                        PetHorizontalCard(pet = pet)
                    }
                }
            }
        }
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
    val vaccineDate = remember(nextVaccine?.dateTimeMillis) {
        nextVaccine?.dateTimeMillis?.toShortDate() ?: "--"
    }
    val consultDate = remember(nextConsultation?.dateTimeMillis) {
        nextConsultation?.dateTimeMillis?.toShortDate() ?: "--"
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            StatItem(
                icon     = Icons.Rounded.Pets,
                label    = "Total de pets",
                value    = if (petCount == 0) "--" else "$petCount",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier  = Modifier.height(52.dp),
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                thickness = 1.dp,
            )
            StatItem(
                icon     = Icons.Rounded.Vaccines,
                label    = "Próxima vacina",
                value    = vaccineDate,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier  = Modifier.height(52.dp),
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                thickness = 1.dp,
            )
            StatItem(
                icon     = Icons.Rounded.MedicalServices,
                label    = "Próxima consulta",
                value    = consultDate,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = OrangePrimary,
            modifier           = Modifier.size(26.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = if (value == "--")
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
            else
                OrangePrimary,
        )
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
            textAlign = TextAlign.Center,
            maxLines  = 2,
        )
    }
}

// ─── Card de dica do dia ──────────────────────────────────────────────────────

@Composable
private fun TipCard(species: String?, modifier: Modifier = Modifier) {
    val tip = remember(species) { PetCareTips.getTodayTip(species) }
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(
            containerColor = OrangePrimary.copy(alpha = 0.09f),
        ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Mascote circular
            Image(
                painter            = painterResource(R.drawable.mel_avatar_pequeno),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.12f), CircleShape),
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = "Dica do dia",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = OrangePrimary,
                )
                Text(
                    text       = tip,
                    style      = MaterialTheme.typography.bodySmall,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

// ─── Card horizontal de pet — redesenhado sem círculo ────────────────────────

@Composable
private fun PetHorizontalCard(pet: Pet) {
    val context      = LocalContext.current
    val speciesIcon  = remember(pet.species) { speciesDrawable(pet.species) }
    val hasRealPhoto = remember(pet.photoPath) {
        pet.photoPath.isNotEmpty() && File(pet.photoPath).exists()
    }
    val ageLabel = remember(pet.birthDate, pet.approximateAge) { petAgeLabel(pet) }

    Card(
        modifier  = Modifier.width(148.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Área da foto — retangular, sem círculo ────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (hasRealPhoto) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(pet.photoPath))
                            .size(300)
                            .scale(Scale.FILL)
                            .crossfade(true)
                            .build(),
                        contentDescription = pet.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    // Sem foto: fundo laranja suave + ícone da espécie centralizado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(OrangePrimary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter            = painterResource(speciesIcon),
                            contentDescription = pet.species,
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier.size(64.dp),
                        )
                    }
                }

                // Gradiente inferior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                            ),
                        ),
                )

                // Nome sobre o gradiente
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }

            // ── Idade como badge com ícone ─────────────────────────────────────
            if (ageLabel.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cake,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = ageLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Estado vazio: nenhum pet cadastrado ─────────────────────────────────────

@Composable
private fun EmptyPetsSection(
    onAddPet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier.fillMaxWidth(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter            = painterResource(R.drawable.onboarding_2_meuspets),
            contentDescription = null,
            modifier           = Modifier.fillMaxWidth(0.68f),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "Seus pets estão esperando por você!",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground,
            textAlign  = TextAlign.Center,
        )
        Text(
            text      = "Adicione o primeiro pet e comece a cuidar com muito carinho.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick  = onAddPet,
            modifier = Modifier.fillMaxWidth(0.80f).height(52.dp),
            shape    = RoundedCornerShape(50.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor   = Color.White,
            ),
        ) {
            Icon(
                imageVector        = Icons.Rounded.Pets,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Adicionar meu primeiro pet",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Helpers privados ─────────────────────────────────────────────────────────

private fun Long.toShortDate(): String {
    val sdf = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
    return sdf.format(Date(this))
}

private fun speciesDrawable(species: String): Int =
    when (species.lowercase(Locale.getDefault())) {
        "gato"             -> R.drawable.icone_especie_gato
        "pássaro",
        "passaro"          -> R.drawable.icone_especie_passaro
        "peixe"            -> R.drawable.icone_especie_peixe
        "réptil", "reptil" -> R.drawable.icone_especie_reptil
        "roedor"           -> R.drawable.icone_especie_roedor
        else               -> R.drawable.icone_especie_cachorro
    }

private fun petAgeLabel(pet: Pet): String {
    if (pet.birthDate.isNotBlank()) {
        return try {
            val sdf   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val bd    = sdf.parse(pet.birthDate) ?: return pet.approximateAge.ifBlank { "" }
            val now   = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { time = bd }
            val totalMonths =
                (now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12 +
                        now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
            when {
                totalMonths < 1  -> "< 1 mês"
                totalMonths < 12 -> "$totalMonths ${if (totalMonths == 1) "mês" else "meses"}"
                else             -> {
                    val years  = totalMonths / 12
                    val months = totalMonths % 12
                    buildString {
                        append("$years ${if (years == 1) "ano" else "anos"}")
                        if (months > 0) append(" e $months ${if (months == 1) "mês" else "meses"}")
                    }
                }
            }
        } catch (e: Exception) {
            pet.approximateAge.ifBlank { "" }
        }
    }
    return pet.approximateAge.ifBlank { "" }
}
