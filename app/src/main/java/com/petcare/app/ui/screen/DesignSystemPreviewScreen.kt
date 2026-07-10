package com.petcare.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.PetCareTheme
import com.petcare.app.ui.theme.PillShape
import com.petcare.app.ui.theme.spacing

/**
 * Preview de validação da base de design (seção 3 do SPEC).
 * Não é uma tela final do app — serve apenas para conferir visualmente
 * cores, tipografia, ícones, raio de borda e espaçamento antes de construir
 * telas reais (splash, onboarding, abas). Não referenciado no NavGraph.
 */
@Composable
fun DesignSystemPreviewScreen() {
    val spacing = MaterialTheme.spacing

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            // Header com gradiente laranja — cores do tema
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "PetCare — Base de Design",
                    style = MaterialTheme.typography.headlineSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(horizontal = spacing.sm),
                )
            }

            // Tipografia — Nunito em todos os estilos
            Card(shape = MaterialTheme.shapes.medium) {
                Column(
                    modifier = Modifier.padding(spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Text("Display", style = MaterialTheme.typography.displaySmall)
                    Text("Headline", style = MaterialTheme.typography.headlineMedium)
                    Text("Title", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Body — texto corrido em Nunito, usado na maior parte do app.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text("Label", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Card com raio 16dp (padrão do tema)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.padding(spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text("Card padrão (raio 16dp)", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Ícones Material Symbols Rounded + espaçamento em grade de 8dp.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Botões / pills com raio 24dp (padrão do tema)
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = {}, shape = PillShape) {
                    Icon(Icons.Rounded.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(spacing.xs / 2))
                    Text("Botão pill (24dp)")
                }
                OutlinedButton(onClick = {}, shape = PillShape) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(spacing.xs / 2))
                    Text("Secundário")
                }
            }

            // Escala de espaçamento visual (8/16/24/32/40/48)
            Card(shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(spacing.sm)) {
                    Text("Grade de espaçamento (8dp)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(spacing.xs))
                    listOf(
                        "xs" to spacing.xs,
                        "sm" to spacing.sm,
                        "md" to spacing.md,
                        "lg" to spacing.lg,
                        "xl" to spacing.xl,
                        "xxl" to spacing.xxl,
                    ).forEach { (label, size) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$label (${size.value.toInt()}dp)",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(90.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .width(size)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.xs / 2))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Design System — Tema Claro")
@Composable
private fun DesignSystemPreviewLight() {
    PetCareTheme(darkTheme = false) {
        DesignSystemPreviewScreen()
    }
}

@Preview(showBackground = true, name = "Design System — Tema Escuro")
@Composable
private fun DesignSystemPreviewDark() {
    PetCareTheme(darkTheme = true) {
        DesignSystemPreviewScreen()
    }
}
