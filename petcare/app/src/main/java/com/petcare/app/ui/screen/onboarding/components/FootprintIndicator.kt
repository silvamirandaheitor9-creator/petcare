package com.petcare.app.ui.screen.onboarding.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petcare.app.ui.theme.OrangePrimary

/**
 * Indicador de progresso com pegadas (SPEC seção 5).
 *
 * A pegada da página atual fica preenchida em [activeColor] (laranja) e
 * levemente maior. As demais ficam na cor [inactiveColor] e menores,
 * com animação de escala com mola entre as transições.
 */
@Composable
fun FootprintIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = OrangePrimary,
    inactiveColor: Color = Color(0xFFD9C5BA),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until pageCount) {
            val isActive = i == currentPage
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.70f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "paw_scale_$i",
            )
            PawPrint(
                modifier = Modifier.size((22 * scale).dp),
                color = if (isActive) activeColor else inactiveColor,
            )
            if (i < pageCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

/**
 * Uma pegada de pet desenhada via Canvas:
 *   – oval principal (almofada central)
 *   – 4 almofadas dos dedos dispostas em arco acima do pad
 */
@Composable
private fun PawPrint(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Almofada central — oval na metade inferior
        drawOval(
            color = color,
            topLeft = Offset(w * 0.18f, h * 0.44f),
            size = Size(w * 0.64f, h * 0.50f),
        )

        // Almofadas dos dedos — 4 círculos em arco
        val r = w * 0.12f
        drawCircle(color = color, radius = r, center = Offset(w * 0.14f, h * 0.30f)) // esq ext
        drawCircle(color = color, radius = r, center = Offset(w * 0.37f, h * 0.16f)) // esq int
        drawCircle(color = color, radius = r, center = Offset(w * 0.63f, h * 0.16f)) // dir int
        drawCircle(color = color, radius = r, center = Offset(w * 0.86f, h * 0.30f)) // dir ext
    }
}
