package com.petcare.app.ui.screen.main.diary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.withRotation
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// Carregamento da foto de origem (galeria), respeitando a orientação EXIF
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Decodifica a imagem do [uri] e aplica a rotação indicada pelos metadados
 * EXIF (fotos de câmera costumam vir "de lado" sem essa correção).
 */
fun loadBitmapRespectingExif(context: Context, uri: Uri): Bitmap? {
    val resolver = context.contentResolver
    val original = resolver.openInputStream(uri)?.use { stream ->
        android.graphics.BitmapFactory.decodeStream(stream)
    } ?: return null

    val rotationDegrees = resolver.openInputStream(uri)?.use { stream ->
        val orientation = ExifInterface(stream)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f

    return if (rotationDegrees != 0f) rotateBitmap(original, rotationDegrees) else original
}

// ─────────────────────────────────────────────────────────────────────────────
// Girar e cortar (SPEC 9.8)
// ─────────────────────────────────────────────────────────────────────────────

fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/**
 * Recorta um sub-quadrado de [bitmap] a partir do canto ([left], [top]) com
 * lado [size], garantindo que a região fique dentro dos limites do bitmap.
 */
fun cropBitmapToSquareRegion(bitmap: Bitmap, left: Int, top: Int, size: Int): Bitmap {
    val safeSize = size.coerceIn(1, minOf(bitmap.width, bitmap.height))
    val safeLeft = left.coerceIn(0, bitmap.width - safeSize)
    val safeTop = top.coerceIn(0, bitmap.height - safeSize)
    return Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeSize, safeSize)
}

// ─────────────────────────────────────────────────────────────────────────────
// Filtros e ajustes manuais (SPEC 9.8-9.9) — matrizes de cor 4x5 (formato
// Android: 4 linhas x 5 colunas, últimas colunas = translação)
// ─────────────────────────────────────────────────────────────────────────────

private fun identityColorMatrix(): FloatArray = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private fun saturationColorMatrix(saturation: Float): FloatArray {
    val lumR = 0.213f
    val lumG = 0.715f
    val lumB = 0.072f
    val sr = (1 - saturation) * lumR
    val sg = (1 - saturation) * lumG
    val sb = (1 - saturation) * lumB
    return floatArrayOf(
        sr + saturation, sg, sb, 0f, 0f,
        sr, sg + saturation, sb, 0f, 0f,
        sr, sg, sb + saturation, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
}

private fun contrastBrightnessColorMatrix(contrast: Float, brightness: Float): FloatArray {
    val translate = (1 - contrast) * 127.5f + brightness
    return floatArrayOf(
        contrast, 0f, 0f, 0f, translate,
        0f, contrast, 0f, 0f, translate,
        0f, 0f, contrast, 0f, translate,
        0f, 0f, 0f, 1f, 0f,
    )
}

/** Combina duas matrizes de cor: resultado equivale a aplicar [m1] e depois [m2]. */
private fun multiplyColorMatrices(m2: FloatArray, m1: FloatArray): FloatArray {
    val result = FloatArray(20)
    for (row in 0 until 4) {
        for (col in 0 until 4) {
            var sum = 0f
            for (k in 0 until 4) {
                sum += m2[row * 5 + k] * m1[k * 5 + col]
            }
            result[row * 5 + col] = sum
        }
        var translation = m2[row * 5 + 4]
        for (k in 0 until 4) {
            translation += m2[row * 5 + k] * m1[k * 5 + 4]
        }
        result[row * 5 + 4] = translation
    }
    return result
}

private fun presetColorMatrix(filter: DiaryPhotoFilter): FloatArray = when (filter) {
    DiaryPhotoFilter.NORMAL -> identityColorMatrix()
    DiaryPhotoFilter.VIVID -> multiplyColorMatrices(
        saturationColorMatrix(1.35f),
        contrastBrightnessColorMatrix(1.12f, 4f),
    )
    DiaryPhotoFilter.SOFT -> multiplyColorMatrices(
        saturationColorMatrix(0.82f),
        contrastBrightnessColorMatrix(0.9f, 14f),
    )
}

/**
 * Monta a matriz de cor final combinando o filtro escolhido com os sliders
 * manuais de brilho/contraste/saturação (SPEC 9.8-9.9). O mesmo array de 20
 * valores é usado tanto na prévia (Compose `ColorFilter.colorMatrix`) quanto
 * na exportação final (`ColorMatrixColorFilter`), garantindo que o que o
 * usuário vê é exatamente o que é salvo.
 */
fun buildDiaryColorMatrix(filter: DiaryPhotoFilter, adjustments: DiaryColorAdjustments): FloatArray {
    val preset = presetColorMatrix(filter)
    val userContrast = 1f + (adjustments.contrast / 100f) * 0.6f
    val userBrightness = (adjustments.brightness / 100f) * 90f
    val userSaturation = 1f + (adjustments.saturation / 100f)

    val afterContrastBrightness = multiplyColorMatrices(
        contrastBrightnessColorMatrix(userContrast, userBrightness),
        preset,
    )
    return multiplyColorMatrices(saturationColorMatrix(userSaturation), afterContrastBrightness)
}

// ─────────────────────────────────────────────────────────────────────────────
// Adesivos temáticos desenhados nativamente (SPEC 9.10) — sem depender de
// nenhuma imagem do pacote, já que não existe nenhum arquivo de patinha,
// coração ou moldura polaroid entre os assets do app (decisão confirmada com
// o usuário). Uma única função de desenho é usada tanto na prévia em Compose
// (via `nativeCanvas`) quanto na exportação final, para os dois ficarem iguais.
// ─────────────────────────────────────────────────────────────────────────────

private const val PAW_COLOR = 0xFFFF7A3D.toInt()
private const val HEART_COLOR = 0xFFFF5C7A.toInt()

fun drawPawSticker(canvas: Canvas, cx: Float, cy: Float, radius: Float, colorArgb: Int = PAW_COLOR) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorArgb }
    // Almofada principal (parte inferior, mais larga)
    canvas.drawOval(RectF(cx - radius, cy - radius * 0.15f, cx + radius, cy + radius), paint)
    // Quatro dedos em leque, na parte superior
    val toeRadius = radius * 0.42f
    val toePositions = listOf(
        -0.62f to -0.55f,
        -0.22f to -0.85f,
        0.22f to -0.85f,
        0.62f to -0.55f,
    )
    for ((dx, dy) in toePositions) {
        canvas.drawCircle(cx + dx * radius, cy + dy * radius, toeRadius, paint)
    }
}

fun drawHeartSticker(canvas: Canvas, cx: Float, cy: Float, radius: Float, colorArgb: Int = HEART_COLOR) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorArgb }
    val path = Path()
    val topY = cy - radius * 0.35f
    path.moveTo(cx, cy + radius * 0.85f)
    path.cubicTo(
        cx - radius * 1.3f, cy + radius * 0.05f,
        cx - radius * 1.05f, cy - radius * 0.95f,
        cx, topY,
    )
    path.cubicTo(
        cx + radius * 1.05f, cy - radius * 0.95f,
        cx + radius * 1.3f, cy + radius * 0.05f,
        cx, cy + radius * 0.85f,
    )
    path.close()
    canvas.drawPath(path, paint)
}

/** Desenha uma borda branca ao redor da foto, no estilo polaroid (margem maior embaixo). */
private fun applyPolaroidFrame(source: Bitmap): Bitmap {
    val border = (source.width * 0.06f).toInt().coerceAtLeast(8)
    val bottomExtra = (source.width * 0.16f).toInt()
    val outWidth = source.width + border * 2
    val outHeight = source.height + border * 2 + bottomExtra
    val framed = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(framed)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(source, border.toFloat(), border.toFloat(), null)
    return framed
}

// ─────────────────────────────────────────────────────────────────────────────
// Texto sobre a imagem (SPEC 9.11)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A tipografia Nunito do app é uma Google Font baixável (SPEC 1.7), carregada
 * de forma assíncrona apenas dentro do Compose. Fora da composição — como no
 * canvas nativo usado para exportar a imagem final — não há uma forma
 * confiável de buscá-la de forma síncrona, então o texto exportado usa uma
 * fonte do sistema com peso/traço próximo (arredondada e em negrito) para não
 * arriscar travar a exportação. A prévia em tela, essa sim, usa a Nunito real.
 */
private fun exportTypeface(): Typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)

// ─────────────────────────────────────────────────────────────────────────────
// Composição final + regra do fundo branco antes do JPEG (SPEC 17.3)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renderiza a foto final: base (com filtro/ajustes aplicados) + adesivos +
 * textos + moldura polaroid opcional. Ainda pode conter transparência nas
 * bordas se a foto de origem tiver canal alfa — por isso o fundo branco é
 * aplicado depois, em [saveDiaryPhotoJpeg], nunca aqui.
 */
fun renderFinalDiaryPhoto(
    baseBitmap: Bitmap,
    colorMatrixValues: FloatArray,
    stickers: List<DiaryStickerItem>,
    texts: List<DiaryTextItem>,
    withPolaroidFrame: Boolean,
): Bitmap {
    val size = minOf(baseBitmap.width, baseBitmap.height)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(colorMatrixValues)
    }
    canvas.drawBitmap(baseBitmap, 0f, 0f, basePaint)

    stickers.forEach { sticker ->
        val cx = sticker.offsetFraction.x * size
        val cy = sticker.offsetFraction.y * size
        val radius = size * 0.09f * sticker.scale
        canvas.withRotation(sticker.rotationDegrees, cx, cy) {
            when (sticker.type) {
                DiaryStickerType.PAW -> drawPawSticker(this, cx, cy, radius)
                DiaryStickerType.HEART -> drawHeartSticker(this, cx, cy, radius)
            }
        }
    }

    val typeface = exportTypeface()
    texts.forEach { item ->
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.color = item.color.toArgb()
            textSize = size * 0.065f * item.scale
            textAlign = Paint.Align.CENTER
            setShadowLayer(size * 0.012f, 0f, 0f, Color.BLACK)
        }
        val cx = item.offsetFraction.x * size
        val cy = item.offsetFraction.y * size
        canvas.drawText(item.text, cx, cy, textPaint)
    }

    return if (withPolaroidFrame) applyPolaroidFrame(output) else output
}

/**
 * Regra da SPEC 17.3: desenha a imagem sobre um fundo branco sólido antes de
 * comprimir em JPEG, para nunca deixar uma borda preta onde havia
 * transparência (o formato JPEG não tem canal alfa).
 */
fun flattenOnWhiteBackground(bitmap: Bitmap): Bitmap {
    val flattened = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(flattened)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    return flattened
}

/** Salva a foto final do Diário em armazenamento interno (SPEC 17.1) como JPEG. */
fun saveDiaryPhotoJpeg(context: Context, bitmap: Bitmap): String {
    val dir = File(context.filesDir, "diary_photos").apply { if (!exists()) mkdirs() }
    val file = File(dir, "diary_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
    val flattened = flattenOnWhiteBackground(bitmap)
    FileOutputStream(file).use { out ->
        flattened.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }
    return file.absolutePath
}
