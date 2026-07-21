package com.petcare.app.ui.screen.main.diary

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─── Tags de atividade / humor ────────────────────────────────────────────────

private data class ActivityTag(val emoji: String, val label: String)

private val ACTIVITY_TAGS = listOf(
    ActivityTag("🐾", "Passeio"),
    ActivityTag("🎾", "Brincadeira"),
    ActivityTag("🛁", "Banho"),
    ActivityTag("🏥", "Consulta"),
    ActivityTag("💛", "Carinho"),
    ActivityTag("📸", "Momento especial"),
)

/**
 * Tela de nova entrada no Diário.
 * Fluxo: galeria → prévia da foto (estilo polaroid) → atividade (mood chips)
 * → legenda → pet → salvar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryAddEntryScreen(
    imageUri: Uri,
    pets: ImmutableList<Pet>,
    onDismiss: () -> Unit,
    onSave: (petId: Long, photoPath: String, caption: String) -> Unit,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var caption        by rememberSaveable { mutableStateOf("") }
    var selectedPetId  by remember(pets) { mutableStateOf(pets.firstOrNull()?.id) }
    var selectedTagIdx by remember { mutableStateOf<Int?>(null) }
    var isSaving       by remember { mutableStateOf(false) }

    fun handleBack() { if (!isSaving) onDismiss() }
    BackHandler(onBack = ::handleBack)

    // Ao selecionar uma tag, preenche a legenda com sugestão se estiver vazia
    fun onTagSelected(index: Int) {
        if (selectedTagIdx == index) {
            selectedTagIdx = null
        } else {
            selectedTagIdx = index
            if (caption.isBlank()) {
                val tag = ACTIVITY_TAGS[index]
                caption = "${tag.emoji} ${tag.label}"
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            // ── TopAppBar com gradiente laranja (padrão do app) ───────────────
            TopAppBar(
                title = {
                    Text(
                        text       = "Nova entrada",
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::handleBack, enabled = !isSaving) {
                        Icon(Icons.Rounded.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrangeGradStart),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                // ── Prévia da foto com efeito polaroid ────────────────────────
                Box(
                    modifier          = Modifier.fillMaxWidth(),
                    contentAlignment  = Alignment.Center,
                ) {
                    Card(
                        modifier  = Modifier
                            .fillMaxWidth(0.78f)
                            .shadow(10.dp, RoundedCornerShape(4.dp)),
                        shape     = RoundedCornerShape(4.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .padding(bottom = 24.dp),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUri)
                                    .size(800)
                                    .scale(Scale.FILL)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto selecionada",
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(2.dp)),
                            )
                        }
                    }
                }

                // ── Tags de atividade ─────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text       = "Qual foi o momento?",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ACTIVITY_TAGS.forEachIndexed { index, tag ->
                            val isSelected = selectedTagIdx == index
                            FilterChip(
                                selected = isSelected,
                                onClick  = { onTagSelected(index) },
                                label    = {
                                    Text(
                                        text       = "${tag.emoji} ${tag.label}",
                                        fontWeight = if (isSelected) FontWeight.SemiBold
                                                     else FontWeight.Normal,
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimary.copy(alpha = 0.15f),
                                    selectedLabelColor     = OrangePrimary,
                                ),
                            )
                        }
                    }
                }

                // ── Campo de legenda ──────────────────────────────────────────
                OutlinedTextField(
                    value         = caption,
                    onValueChange = { if (it.length <= 140) caption = it },
                    label         = { Text("Legenda (opcional)") },
                    supportingText = { Text("${caption.length}/140") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    minLines      = 2,
                )

                // ── Seletor de pet ────────────────────────────────────────────
                when {
                    pets.size > 1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text       = "Pet",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onBackground,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                pets.forEach { pet ->
                                    FilterChip(
                                        selected = pet.id == selectedPetId,
                                        onClick  = { selectedPetId = pet.id },
                                        label    = { Text(pet.name) },
                                        colors   = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = OrangePrimary.copy(alpha = 0.16f),
                                            selectedLabelColor     = OrangePrimary,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    pets.isEmpty() -> {
                        Text(
                            text  = "Cadastre um pet antes de adicionar uma entrada no Diário.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    // pets.size == 1: selecionado automaticamente — não exibe chips
                }

                // ── Botão Salvar ──────────────────────────────────────────────
                Button(
                    onClick = {
                        val petId = selectedPetId ?: return@Button
                        isSaving = true
                        scope.launch {
                            try {
                                val photoPath = withContext(Dispatchers.IO) {
                                    val bitmap = loadBitmapRespectingExif(context, imageUri)
                                        ?: error("Não foi possível carregar a foto")
                                    saveDiaryPhotoJpeg(context, bitmap)
                                }
                                onSave(petId, photoPath, caption.trim())
                            } catch (_: Exception) {
                                isSaving = false
                                onDismiss()
                            }
                        }
                    },
                    enabled  = !isSaving && pets.isNotEmpty(),
                    colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape    = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Salvar no Diário", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
