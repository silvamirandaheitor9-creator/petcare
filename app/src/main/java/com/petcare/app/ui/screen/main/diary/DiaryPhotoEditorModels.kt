package com.petcare.app.ui.screen.main.diary

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Filtros prontos do editor de fotos do Diário (SPEC 9.8).
 * Cada filtro é uma predefinição de matriz de cor; os sliders manuais
 * (SPEC 9.9) se combinam por cima do filtro escolhido.
 */
enum class DiaryPhotoFilter(val label: String) {
    NORMAL("Normal"),
    VIVID("Vívido"),
    SOFT("Suave"),
}

/**
 * Ajustes manuais de cor (SPEC 9.9). Cada slider vai de -100 a 100,
 * onde 0 é neutro (sem alteração em relação ao filtro escolhido).
 */
data class DiaryColorAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
)

/** Tipos de adesivo temático disponíveis (SPEC 9.10). */
enum class DiaryStickerType {
    PAW,
    HEART,
}

/**
 * Um adesivo (patinha/coração) posicionado sobre a foto.
 * [offsetFraction] é relativo ao canvas quadrado final (0..1 em cada eixo),
 * garantindo que a posição escolhida na prévia corresponda à posição na
 * imagem final exportada, independentemente da resolução de cada uma.
 */
data class DiaryStickerItem(
    val id: Long,
    val type: DiaryStickerType,
    val offsetFraction: Offset = Offset(0.5f, 0.5f),
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
)

/** Um texto livre posicionado sobre a foto (SPEC 9.11), na tipografia do app. */
data class DiaryTextItem(
    val id: Long,
    val text: String,
    val offsetFraction: Offset = Offset(0.5f, 0.5f),
    val color: Color = Color.White,
    val scale: Float = 1f,
)
