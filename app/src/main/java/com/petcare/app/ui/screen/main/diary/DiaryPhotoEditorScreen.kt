package com.petcare.app.ui.screen.main.diary

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.theme.spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Editor de fotos embutido do Diário (SPEC 9.8-9.11), aberto pelo botão "+"
 * depois que o usuário escolhe uma foto da galeria. Fluxo em duas etapas:
 * 1) Cortar e girar; 2) Filtros/ajustes/adesivos/texto + legenda e salvar.
 *
 * Ao salvar, a imagem final é desenhada sobre um fundo branco sólido antes de
 * ser comprimida em JPEG (SPEC 17.3), evitando fundo preto em fotos com
 * transparência.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryPhotoEditorScreen(
    imageUri: Uri,
    pets: List<Pet>,
    onDismiss: () -> Unit,
    onSave: (petId: Long, photoPath: String, caption: String) -> Unit,
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

    var step by remember { mutableStateOf(0) } // 0 = cortar/girar, 1 = ajustar e salvar
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var filter by remember { mutableStateOf(DiaryPhotoFilter.NORMAL) }
    var adjustments by remember { mutableStateOf(DiaryColorAdjustments()) }
    var showPolaroidFrame by remember { mutableStateOf(false) }
    val stickers = remember { mutableStateListOf<DiaryStickerItem>() }
    val texts = remember { mutableStateListOf<DiaryTextItem>() }
    var selectedOverlayId by remember { mutableStateOf<Long?>(null) }
    var nextOverlayId by remember { mutableStateOf(1L) }
    var textDialogVisible by remember { mutableStateOf(false) }

    var caption by rememberSaveable { mutableStateOf("") }
    var selectedPetId by remember(pets) { mutableStateOf(pets.firstOrNull()?.id) }
    var isSaving by remember { mutableStateOf(false) }

    fun clampFraction(offset: Offset) = Offset(offset.x.coerceIn(0f, 1f), offset.y.coerceIn(0f, 1f))

    fun handleBack() {
        when {
            isSaving -> Unit
            step == 1 -> step = 0
            else -> onDismiss()
        }
    }
    BackHandler(onBack = ::handleBack)

    // Tela cheia normal do NavGraph (deixou de ser Dialog — SPEC 9.8-9.11 bug fix:
    // Dialog tem propagação de WindowInsets diferente/menos confiável em algumas
    // janelas do que uma composable de rota normal, o que escondia a Row de baixo
    // atrás da barra de gestos mesmo com systemBarsPadding aplicado).
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            TopAppBar(
                title = { Text(if (step == 0) "Cortar e girar" else "Ajustar foto") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack, enabled = !isSaving) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )

            when {
                isLoadingSource -> LoadingBox()
                sourceBitmap == null -> ErrorBox(onDismiss = onDismiss)
                step == 0 -> CropRotateStep(
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
                        croppedBitmap = cropped
                        step = 1
                    },
                )
                else -> DecorateAndSaveStep(
                    baseBitmap = croppedBitmap!!,
                    filter = filter,
                    onFilterChange = { filter = it },
                    adjustments = adjustments,
                    onAdjustmentsChange = { adjustments = it },
                    showPolaroidFrame = showPolaroidFrame,
                    onTogglePolaroidFrame = { showPolaroidFrame = !showPolaroidFrame },
                    stickers = stickers,
                    texts = texts,
                    selectedOverlayId = selectedOverlayId,
                    onSelectOverlay = { selectedOverlayId = it },
                    onAddSticker = { type ->
                        val jitterX = (Random.nextFloat() - 0.5f) * 0.16f
                        val jitterY = (Random.nextFloat() - 0.5f) * 0.16f
                        stickers.add(
                            DiaryStickerItem(
                                id = nextOverlayId++,
                                type = type,
                                offsetFraction = Offset(0.5f + jitterX, 0.5f + jitterY),
                            ),
                        )
                    },
                    onRequestAddText = { textDialogVisible = true },
                    onRemoveSelected = {
                        selectedOverlayId?.let { id ->
                            stickers.removeAll { it.id == id }
                            texts.removeAll { it.id == id }
                        }
                        selectedOverlayId = null
                    },
                    onDragSticker = { id, delta ->
                        val idx = stickers.indexOfFirst { it.id == id }
                        if (idx >= 0) {
                            val current = stickers[idx]
                            stickers[idx] = current.copy(
                                offsetFraction = clampFraction(current.offsetFraction + delta),
                            )
                        }
                    },
                    onDragText = { id, delta ->
                        val idx = texts.indexOfFirst { it.id == id }
                        if (idx >= 0) {
                            val current = texts[idx]
                            texts[idx] = current.copy(
                                offsetFraction = clampFraction(current.offsetFraction + delta),
                            )
                        }
                    },
                    caption = caption,
                    onCaptionChange = { if (it.length <= 140) caption = it },
                    pets = pets,
                    selectedPetId = selectedPetId,
                    onSelectPet = { selectedPetId = it },
                    isSaving = isSaving,
                    onSave = {
                        val petId = selectedPetId ?: return@DecorateAndSaveStep
                        isSaving = true
                        scope.launch {
                            val photoPath = withContext(Dispatchers.Default) {
                                val matrix = buildDiaryColorMatrix(filter, adjustments)
                                val rendered = renderFinalDiaryPhoto(
                                    baseBitmap = croppedBitmap!!,
                                    colorMatrixValues = matrix,
                                    stickers = stickers.toList(),
                                    texts = texts.toList(),
                                    withPolaroidFrame = showPolaroidFrame,
                                )
                                saveDiaryPhotoJpeg(context, rendered)
                            }
                            isSaving = false
                            onSave(petId, photoPath, caption.trim())
                        }
                    },
                )
            }
        }
    }

    if (textDialogVisible) {
        AddTextDialog(
            onDismiss = { textDialogVisible = false },
            onConfirm = { text, color ->
                texts.add(
                    DiaryTextItem(
                        id = nextOverlayId++,
                        text = text,
                        color = color,
                    ),
                )
                textDialogVisible = false
            },
        )
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = OrangePrimary)
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

// ─── Etapa 1: cortar e girar (SPEC 9.8) ──────────────────────────────────────

@Composable
private fun CropRotateStep(
    workingBitmap: Bitmap,
    onRotate: () -> Unit,
    onCropApplied: (Bitmap) -> Unit,
) {
    var frameSizePx by remember { mutableStateOf(0f) }
    var panOffset by remember(workingBitmap) { mutableStateOf(Offset.Zero) }
    var zoom by remember(workingBitmap) { mutableStateOf(1f) }

    val imageBitmap = remember(workingBitmap) { workingBitmap.asImageBitmap() }

    val baseScale = remember(workingBitmap, frameSizePx) {
        if (frameSizePx <= 0f) 1f
        else maxOf(frameSizePx / workingBitmap.width, frameSizePx / workingBitmap.height)
    }
    val totalScale = baseScale * zoom

    fun clampPan(offset: Offset, scale: Float): Offset {
        val displayedWidth = workingBitmap.width * scale
        val displayedHeight = workingBitmap.height * scale
        val maxX = ((displayedWidth - frameSizePx) / 2f).coerceAtLeast(0f)
        val maxY = ((displayedHeight - frameSizePx) / 2f).coerceAtLeast(0f)
        return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .aspectRatio(1f)
                    .onSizeChanged { frameSizePx = it.width.toFloat() }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .pointerInput(workingBitmap, frameSizePx) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            val newZoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                            zoom = newZoom
                            panOffset = clampPan(panOffset + pan, baseScale * newZoom)
                        }
                    },
            ) {
                val dstWidth = (workingBitmap.width * totalScale).roundToInt()
                val dstHeight = (workingBitmap.height * totalScale).roundToInt()
                val dstOffset = IntOffset(
                    x = (size.width / 2f - dstWidth / 2f + panOffset.x).roundToInt(),
                    y = (size.height / 2f - dstHeight / 2f + panOffset.y).roundToInt(),
                )
                drawImage(
                    image = imageBitmap,
                    dstOffset = dstOffset,
                    dstSize = IntSize(dstWidth, dstHeight),
                )
            }
        }
        Text(
            text = "Arraste para posicionar e use pinça para dar zoom",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.sm),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onRotate) {
                Icon(Icons.Rounded.RotateRight, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Girar")
            }
            Button(
                onClick = {
                    val cropSizePx = (frameSizePx / totalScale)
                    val leftPx = (workingBitmap.width - cropSizePx) / 2f - panOffset.x / totalScale
                    val topPx = (workingBitmap.height - cropSizePx) / 2f - panOffset.y / totalScale
                    val cropped = cropBitmapToSquareRegion(
                        bitmap = workingBitmap,
                        left = leftPx.roundToInt(),
                        top = topPx.roundToInt(),
                        size = cropSizePx.roundToInt(),
                    )
                    onCropApplied(cropped)
                },
                modifier = Modifier.height(56.dp),
            ) {
                Text("Avançar")
            }
        }
    }
}

// ─── Etapa 2: filtros, ajustes, adesivos, texto, legenda e salvar ───────────

@Composable
private fun DecorateAndSaveStep(
    baseBitmap: Bitmap,
    filter: DiaryPhotoFilter,
    onFilterChange: (DiaryPhotoFilter) -> Unit,
    adjustments: DiaryColorAdjustments,
    onAdjustmentsChange: (DiaryColorAdjustments) -> Unit,
    showPolaroidFrame: Boolean,
    onTogglePolaroidFrame: () -> Unit,
    stickers: List<DiaryStickerItem>,
    texts: List<DiaryTextItem>,
    selectedOverlayId: Long?,
    onSelectOverlay: (Long?) -> Unit,
    onAddSticker: (DiaryStickerType) -> Unit,
    onRequestAddText: () -> Unit,
    onRemoveSelected: () -> Unit,
    onDragSticker: (id: Long, delta: Offset) -> Unit,
    onDragText: (id: Long, delta: Offset) -> Unit,
    caption: String,
    onCaptionChange: (String) -> Unit,
    pets: List<Pet>,
    selectedPetId: Long?,
    onSelectPet: (Long) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit,
) {
    val imageBitmap = remember(baseBitmap) { baseBitmap.asImageBitmap() }
    val colorMatrixValues = remember(filter, adjustments) { buildDiaryColorMatrix(filter, adjustments) }
    var canvasSizePx by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        // ── Prévia com adesivos e texto arrastáveis ──────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(16.dp))
                .onSizeChanged { canvasSizePx = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onSelectOverlay(null) })
                },
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Prévia da foto",
                colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrixValues)),
                modifier = Modifier.fillMaxSize(),
            )
            if (showPolaroidFrame) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(width = 10.dp, color = Color.White),
                )
            }
            stickers.forEach { sticker ->
                StickerOverlay(
                    sticker = sticker,
                    canvasSizePx = canvasSizePx,
                    isSelected = sticker.id == selectedOverlayId,
                    onSelect = { onSelectOverlay(sticker.id) },
                    onDrag = { delta -> onDragSticker(sticker.id, delta) },
                )
            }
            texts.forEach { textItem ->
                TextOverlay(
                    item = textItem,
                    canvasSizePx = canvasSizePx,
                    isSelected = textItem.id == selectedOverlayId,
                    onSelect = { onSelectOverlay(textItem.id) },
                    onDrag = { delta -> onDragText(textItem.id, delta) },
                )
            }
        }

        if (selectedOverlayId != null) {
            TextButton(onClick = onRemoveSelected) {
                Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(4.dp))
                Text("Remover selecionado", color = MaterialTheme.colorScheme.error)
            }
        }

        // ── Filtros (SPEC 9.8) ────────────────────────────────────────────────
        Text("Filtro", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            DiaryPhotoFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { onFilterChange(option) },
                    label = { Text(option.label) },
                )
            }
        }

        // ── Sliders de brilho/contraste/saturação (SPEC 9.9) ─────────────────
        Text("Ajustes", style = MaterialTheme.typography.titleSmall)
        AdjustmentSlider(
            label = "Brilho",
            value = adjustments.brightness,
            onValueChange = { onAdjustmentsChange(adjustments.copy(brightness = it)) },
        )
        AdjustmentSlider(
            label = "Contraste",
            value = adjustments.contrast,
            onValueChange = { onAdjustmentsChange(adjustments.copy(contrast = it)) },
        )
        AdjustmentSlider(
            label = "Saturação",
            value = adjustments.saturation,
            onValueChange = { onAdjustmentsChange(adjustments.copy(saturation = it)) },
        )

        // ── Adesivos temáticos + texto (SPEC 9.10-9.11) ──────────────────────
        Text("Adesivos e texto", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            FilterChip(
                selected = false,
                onClick = { onAddSticker(DiaryStickerType.PAW) },
                label = { Text("Patinha") },
                leadingIcon = { Icon(Icons.Rounded.Pets, contentDescription = null) },
            )
            FilterChip(
                selected = false,
                onClick = { onAddSticker(DiaryStickerType.HEART) },
                label = { Text("Coração") },
                leadingIcon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
            )
            FilterChip(
                selected = showPolaroidFrame,
                onClick = onTogglePolaroidFrame,
                label = { Text("Moldura") },
            )
            FilterChip(
                selected = false,
                onClick = onRequestAddText,
                label = { Text("Texto") },
                leadingIcon = { Icon(Icons.Rounded.TextFields, contentDescription = null) },
            )
        }

        // ── Legenda ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value = caption,
            onValueChange = onCaptionChange,
            label = { Text("Legenda (opcional)") },
            supportingText = { Text("${caption.length}/140") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        // ── Seleção de pet (apenas se houver mais de um) ─────────────────────
        if (pets.size > 1) {
            Text("Pet", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                pets.forEach { pet ->
                    FilterChip(
                        selected = pet.id == selectedPetId,
                        onClick = { onSelectPet(pet.id) },
                        label = { Text(pet.name) },
                    )
                }
            }
        } else if (pets.isEmpty()) {
            Text(
                text = "Cadastre um pet antes de adicionar uma entrada no Diário.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = onSave,
            enabled = !isSaving && pets.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text("Salvar no Diário", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
    }
}

@Composable
private fun AdjustmentSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -100f..100f,
            colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
        )
    }
}

@Composable
private fun StickerOverlay(
    sticker: DiaryStickerItem,
    canvasSizePx: Float,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Offset) -> Unit,
) {
    val sizeDp = 56.dp
    var sizePx by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (sticker.offsetFraction.x * canvasSizePx - sizePx / 2).roundToInt(),
                    (sticker.offsetFraction.y * canvasSizePx - sizePx / 2).roundToInt(),
                )
            }
            .size(sizeDp)
            .onSizeChanged { sizePx = it.width.toFloat() }
            .then(
                if (isSelected) Modifier.border(2.dp, OrangePrimary, CircleShape) else Modifier,
            )
            .pointerInput(sticker.id, canvasSizePx) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                ) { change, dragAmount ->
                    change.consume()
                    if (canvasSizePx > 0f) {
                        onDrag(Offset(dragAmount.x / canvasSizePx, dragAmount.y / canvasSizePx))
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f * 0.85f * sticker.scale
                when (sticker.type) {
                    DiaryStickerType.PAW -> drawPawSticker(nativeCanvas, cx, cy, radius)
                    DiaryStickerType.HEART -> drawHeartSticker(nativeCanvas, cx, cy, radius)
                }
            }
        }
    }
}

@Composable
private fun TextOverlay(
    item: DiaryTextItem,
    canvasSizePx: Float,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Offset) -> Unit,
) {
    var measuredSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (item.offsetFraction.x * canvasSizePx - measuredSize.width / 2f).roundToInt(),
                    (item.offsetFraction.y * canvasSizePx - measuredSize.height / 2f).roundToInt(),
                )
            }
            .onSizeChanged { measuredSize = it }
            .then(
                if (isSelected) Modifier.border(1.dp, OrangePrimary, RoundedCornerShape(4.dp)) else Modifier,
            )
            .pointerInput(item.id, canvasSizePx) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                ) { change, dragAmount ->
                    change.consume()
                    if (canvasSizePx > 0f) {
                        onDrag(Offset(dragAmount.x / canvasSizePx, dragAmount.y / canvasSizePx))
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = item.color,
            modifier = Modifier.graphicsLayer {
                scaleX = item.scale
                scaleY = item.scale
            },
        )
    }
}

@Composable
private fun AddTextDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Color) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) }
    val colorOptions = listOf(Color.White, Color.Black, OrangePrimary)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar texto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 40) text = it },
                    label = { Text("Texto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                    colorOptions.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) OrangePrimary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                )
                                .pointerInput(color) {
                                    detectTapGestures(onTap = { selectedColor = color })
                                },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim(), selectedColor) },
                enabled = text.isNotBlank(),
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
