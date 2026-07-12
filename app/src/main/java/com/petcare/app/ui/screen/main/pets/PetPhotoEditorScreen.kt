package com.petcare.app.ui.screen.main.pets

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.petcare.app.ui.screen.main.diary.CropRotateStep
import com.petcare.app.ui.screen.main.diary.loadBitmapRespectingExif
import com.petcare.app.ui.screen.main.diary.rotateBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

/**
 * Editor de foto de perfil do pet (SPEC §11 — parte 2): mesmo padrão de tela
 * cheia normal do NavGraph do editor do Diário, mas só com a etapa de
 * cortar/girar (`CropRotateStep`, reaproveitado de lá) — sem filtros,
 * adesivos ou texto, que não fazem sentido para um avatar circular.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetPhotoEditorScreen(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onSave: (photoPath: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var isLoadingSource by remember(imageUri) { mutableStateOf(true) }
    LaunchedEffect(imageUri) {
        isLoadingSource = true
        sourceBitmap = withContext(Dispatchers.IO) { loadBitmapRespectingExif(context, imageUri) }
        isLoadingSource = false
    }

    var isSaving by remember { mutableStateOf(false) }

    fun handleBack() {
        if (!isSaving) onDismiss()
    }
    BackHandler(onBack = ::handleBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            TopAppBar(
                title = { Text("Foto do pet") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack, enabled = !isSaving) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )

            when {
                isLoadingSource || isSaving -> LoadingBox()
                sourceBitmap == null -> ErrorBox(onDismiss = onDismiss)
                else -> CropRotateStep(
                    workingBitmap = sourceBitmap!!,
                    onRotate = {
                        scope.launch {
                            val rotated = withContext(Dispatchers.Default) {
                                rotateBitmap(sourceBitmap!!, 90f)
                            }
                            sourceBitmap = rotated
                        }
                    },
                    onCropApplied = { cropped ->
                        isSaving = true
                        scope.launch {
                            val photoPath = withContext(Dispatchers.Default) {
                                savePetPhotoJpeg(context, cropped)
                            }
                            isSaving = false
                            onSave(photoPath)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorBox(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Não foi possível abrir essa foto.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onDismiss) { Text("Voltar") }
        }
    }
}
