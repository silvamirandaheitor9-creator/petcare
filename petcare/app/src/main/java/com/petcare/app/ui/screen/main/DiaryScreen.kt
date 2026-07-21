package com.petcare.app.ui.screen.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.petcare.app.R
import com.petcare.app.data.db.entity.DiaryEntry
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.DiaryViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// ─── Ponto de entrada da aba Diário ──────────────────────────────────────────

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(),
    showAddEntryPlaceholder: Boolean = false,
    onDismissAddEntryPlaceholder: () -> Unit = {},
    onNavigateToPhotoEditor: (Uri) -> Unit = {},
    onEditEntry: (Long) -> Unit = {},
) {
    val entriesState = viewModel.entries.collectAsState()
    val petsState    = viewModel.pets.collectAsState()
    val entries by entriesState
    val pets    by petsState

    // ── Fluxo do "+": galeria → editor de fotos ───────────────────────────────
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onNavigateToPhotoEditor(uri)
        onDismissAddEntryPlaceholder()
    }
    LaunchedEffect(showAddEntryPlaceholder) {
        if (showAddEntryPlaceholder) {
            pickImageLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                ),
            )
        }
    }

    var selectedPetId by rememberSaveable { mutableStateOf<Long?>(null) }

    val visibleEntries by remember {
        derivedStateOf {
            val e = entriesState.value
            if (selectedPetId == null) e else e.filter { it.petId == selectedPetId }
        }
    }

    var entryPendingDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    val hasLoadedOnce  = remember { mutableStateOf(false) }
    val knownEntryIds  = remember { mutableSetOf<Long>() }
    LaunchedEffect(entries) {
        if (!hasLoadedOnce.value) {
            knownEntryIds += entries.map { it.id }
            hasLoadedOnce.value = true
        }
    }

    if (entries.isEmpty()) {
        EmptyDiarySection()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        ) {
            if (pets.size > 1) {
                item {
                    PetFilterRow(
                        pets          = pets,
                        selectedPetId = selectedPetId,
                        onSelect      = { selectedPetId = it },
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            items(visibleEntries, key = { it.id }) { entry ->
                val pet = pets.firstOrNull { it.id == entry.petId }
                PolaroidReveal(
                    entryId      = entry.id,
                    hasLoadedOnce = hasLoadedOnce.value,
                    knownEntryIds = knownEntryIds,
                ) {
                    DiaryEntryCard(
                        entry          = entry,
                        petName        = pet?.name ?: "Pet",
                        onDeleteRequest = { entryPendingDelete = entry },
                        onEditRequest   = { onEditEntry(entry.id) },
                        modifier        = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    // ── Confirmação de exclusão ───────────────────────────────────────────────
    entryPendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Excluir registro?") },
            text  = {
                Text("Essa foto e legenda serão removidas do diário. Essa ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    entryPendingDelete = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) { Text("Cancelar") }
            },
        )
    }
}

// ─── Filtro por pet ───────────────────────────────────────────────────────────

@Composable
private fun PetFilterRow(
    pets: ImmutableList<Pet>,
    selectedPetId: Long?,
    onSelect: (Long?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(horizontal = 16.dp),
    ) {
        item {
            FilterChip(
                selected = selectedPetId == null,
                onClick  = { onSelect(null) },
                label    = { Text("Todos") },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary.copy(alpha = 0.16f),
                    selectedLabelColor     = OrangePrimary,
                ),
            )
        }
        items(pets, key = { it.id }) { pet ->
            FilterChip(
                selected = selectedPetId == pet.id,
                onClick  = { onSelect(pet.id) },
                label    = { Text(pet.name) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary.copy(alpha = 0.16f),
                    selectedLabelColor     = OrangePrimary,
                ),
            )
        }
    }
}

// ─── Card de entrada no diário ────────────────────────────────────────────────

@Composable
private fun DiaryEntryCard(
    entry: DiaryEntry,
    petName: String,
    onDeleteRequest: () -> Unit,
    onEditRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dateStr = remember(entry.dateMillis) { entry.dateMillis.toDiaryDate() }

    val imageRequest = remember(entry.photoPath) {
        ImageRequest.Builder(context)
            .data(if (entry.photoPath.isNotEmpty()) File(entry.photoPath) else null)
            .memoryCacheKey(entry.photoPath)
            .diskCacheKey(entry.photoPath)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(720)
            .scale(Scale.FILL)
            .crossfade(true)
            .build()
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            // ── Foto ──────────────────────────────────────────────────────────
            AsyncImage(
                model              = imageRequest,
                contentDescription = "Foto de $petName",
                contentScale       = ContentScale.Crop,
                fallback           = painterResource(R.drawable.avatar_pet_padrao),
                error              = painterResource(R.drawable.avatar_pet_padrao),
                placeholder        = painterResource(R.drawable.avatar_pet_padrao),
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
            )

            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                // ── Cabeçalho: nome do pet + data ─────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text       = "🐾 $petName",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = OrangePrimary,
                    )
                    Text(
                        text  = "·",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                    Text(
                        text  = dateStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }

                // ── Legenda ───────────────────────────────────────────────────
                if (entry.caption.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = entry.caption.take(140),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ── Ações: compartilhar, editar, excluir ──────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { shareDiaryEntry(context, entry, petName, dateStr) }) {
                        Icon(
                            imageVector        = Icons.Rounded.Share,
                            contentDescription = "Compartilhar",
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                        )
                    }
                    IconButton(onClick = onEditRequest) {
                        Icon(
                            imageVector        = Icons.Rounded.Edit,
                            contentDescription = "Editar legenda",
                            tint               = OrangePrimary.copy(alpha = 0.80f),
                        )
                    }
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            imageVector        = Icons.Rounded.Delete,
                            contentDescription = "Excluir",
                            tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compartilha a foto + uma mensagem formatada com o nome do pet, legenda e data.
 * Muito mais criativo do que mandar apenas a foto — cria um mini "cartão de memória".
 */
private fun shareDiaryEntry(
    context: android.content.Context,
    entry: DiaryEntry,
    petName: String,
    dateStr: String,
) {
    if (entry.photoPath.isEmpty()) return
    val file = File(entry.photoPath)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val shareText = buildString {
        appendLine("🐾 Momentos com $petName")
        appendLine()
        if (entry.caption.isNotBlank()) {
            appendLine("\"${entry.caption}\"")
            appendLine()
        }
        appendLine("📅 $dateStr")
        appendLine()
        append("Compartilhado com amor via PetCare 🐾")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar via"))
}

// ─── Animação polaroid ao adicionar entrada ───────────────────────────────────

@Composable
private fun PolaroidReveal(
    entryId: Long,
    hasLoadedOnce: Boolean,
    knownEntryIds: MutableSet<Long>,
    content: @Composable () -> Unit,
) {
    val isNewEntry = remember(entryId) {
        val isNew = hasLoadedOnce && entryId !in knownEntryIds
        knownEntryIds += entryId
        isNew
    }

    val rotation = remember(entryId) { Animatable(if (isNewEntry) -9f else 0f) }
    val scale    = remember(entryId) { Animatable(if (isNewEntry) 0.82f else 1f) }

    LaunchedEffect(entryId) {
        if (isNewEntry) {
            launch {
                rotation.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                )
            }
            launch {
                scale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                )
            }
        }
    }

    Box(
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation.value
            scaleX    = scale.value
            scaleY    = scale.value
        },
    ) {
        content()
    }
}

// ─── Estado vazio ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyDiarySection() {
    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter            = painterResource(R.drawable.vazio_diario),
                contentDescription = null,
                modifier           = Modifier.fillMaxWidth(0.62f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "Seu diário ainda está em branco",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
                textAlign  = TextAlign.Center,
            )
            Text(
                text      = "Toque no + para guardar os primeiros momentos com seu pet.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Helpers privados ─────────────────────────────────────────────────────────

private val DIARY_DATE_SDF = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
private fun Long.toDiaryDate(): String = DIARY_DATE_SDF.format(java.util.Date(this))
