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
import kotlinx.collections.immutable.ImmutableList
import com.petcare.app.ui.theme.OrangePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tela simplificada de nova entrada no Diário (SPEC §9):
 * galeria → prévia da foto → legenda + seletor de pet → salvar.
 *
 * Sem edição de imagem. A regra SPEC 17.3 (fundo branco antes de JPEG)
 * é aplicada dentro de [saveDiaryPhotoJpeg], chamado no momento do salvamento.
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

    var caption       by rememberSaveable { mutableStateOf("") }
    var selectedPetId by remember(pets) { mutableStateOf(pets.firstOrNull()?.id) }
    var isSaving      by remember { mutableStateOf(false) }

    fun handleBack() { if (!isSaving) onDismiss() }
    BackHandler(onBack = ::handleBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            TopAppBar(
                title = { Text("Nova entrada") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack, enabled = !isSaving) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Prévia da foto escolhida na galeria ───────────────────────
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .size(800)
                        .scale(Scale.FILL)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto selecionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                )

                // ── Campo de legenda (até 140 caracteres) ─────────────────────
                OutlinedTextField(
                    value = caption,
                    onValueChange = { if (it.length <= 140) caption = it },
                    label = { Text("Legenda (opcional)") },
                    supportingText = { Text("${caption.length}/140") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                // ── Seletor de pet ────────────────────────────────────────────
                when {
                    pets.size > 1 -> {
                        Text("Pet", style = MaterialTheme.typography.titleSmall)
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
                    pets.isEmpty() -> {
                        Text(
                            text  = "Cadastre um pet antes de adicionar uma entrada no Diário.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    // pets.size == 1: selecionado automaticamente; não exibe chips
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
                    enabled = !isSaving && pets.isNotEmpty(),
                    colors  = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape   = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color    = Color.White,
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
