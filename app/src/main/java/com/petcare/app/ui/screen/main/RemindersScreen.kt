package com.petcare.app.ui.screen.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Repeat
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.R
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.ReminderGroup
import com.petcare.app.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Ponto de entrada da aba Lembretes (SPEC §10 — Parte 1) ──────────────────

@Composable
fun RemindersScreen(
    viewModel: ReminderViewModel = hiltViewModel(),
    onNavigateToNewReminder: (reminderId: Long) -> Unit = {},
) {
    val grouped by viewModel.groupedReminders.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val selectedPetId by viewModel.selectedPetId.collectAsState()
    val historicoExpanded by viewModel.historicoExpanded.collectAsState()

    val isEmpty = grouped.isEmpty()

    if (isEmpty) {
        ReminderEmptyState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {
        // ── Chips de filtro por pet ────────────────────────────────────────────
        if (pets.size > 1) {
            item(key = "pet_filter") {
                PetFilterChips(
                    pets = pets,
                    selectedPetId = selectedPetId,
                    onSelectPet = { viewModel.selectPet(it) },
                )
            }
        }

        // ── Seções: Hoje / Amanhã / Esta semana ───────────────────────────────
        for (group in listOf(
            ReminderGroup.HOJE,
            ReminderGroup.AMANHA,
            ReminderGroup.ESTA_SEMANA,
        )) {
            val items = grouped[group] ?: continue

            item(key = "header_${group.name}") {
                SectionHeader(label = group.label())
            }

            items(items = items, key = { "reminder_${it.id}" }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    pets = pets,
                    onEdit = { onNavigateToNewReminder(reminder.id) },
                    onDelete = { viewModel.deleteReminder(reminder) },
                    onToggleCompleted = { viewModel.toggleCompleted(reminder) },
                )
            }
        }

        // ── Histórico (recolhível) ─────────────────────────────────────────────
        val historico = grouped[ReminderGroup.HISTORICO]
        if (!historico.isNullOrEmpty()) {
            item(key = "header_historico") {
                HistoricoHeader(
                    count = historico.size,
                    expanded = historicoExpanded,
                    onToggle = { viewModel.toggleHistorico() },
                )
            }

            item(key = "historico_content") {
                AnimatedVisibility(
                    visible = historicoExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        historico.forEach { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                pets = pets,
                                onEdit = { onNavigateToNewReminder(reminder.id) },
                                onDelete = { viewModel.deleteReminder(reminder) },
                                onToggleCompleted = { viewModel.toggleCompleted(reminder) },
                                isHistorico = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Estado vazio ─────────────────────────────────────────────────────────────

@Composable
private fun ReminderEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 40.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.vazio_lembretes),
                contentDescription = "Nenhum lembrete",
                modifier = Modifier.fillMaxWidth(0.65f),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = "Nenhum lembrete por aqui ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Toque no + para criar o primeiro lembrete para seus pets.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Chips de filtro por pet ──────────────────────────────────────────────────

@Composable
private fun PetFilterChips(
    pets: List<Pet>,
    selectedPetId: Long?,
    onSelectPet: (Long?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedPetId == null,
                onClick = { onSelectPet(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedPetId == null,
                    selectedBorderColor = OrangePrimary,
                ),
            )
        }
        items(pets) { pet ->
            FilterChip(
                selected = selectedPetId == pet.id,
                onClick = { onSelectPet(pet.id) },
                label = { Text(pet.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedPetId == pet.id,
                    selectedBorderColor = OrangePrimary,
                ),
            )
        }
    }
}

// ─── Cabeçalho de seção ───────────────────────────────────────────────────────

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = OrangePrimary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

// ─── Cabeçalho do Histórico (recolhível) ─────────────────────────────────────

@Composable
private fun HistoricoHeader(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Histórico ($count)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Recolher histórico" else "Expandir histórico",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

// ─── Card de lembrete ─────────────────────────────────────────────────────────

@Composable
private fun ReminderCard(
    reminder: Reminder,
    pets: List<Pet>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    isHistorico: Boolean = false,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val petName = pets.find { it.id == reminder.petId }?.name ?: "Pet"
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(reminder.dateTimeMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Ícone da categoria
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isHistorico || reminder.isCompleted)
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                        else
                            OrangePrimary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(reminder.category.toCategoryDrawable()),
                    contentDescription = reminder.category,
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Conteúdo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (reminder.isCompleted)
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "🐾 $petName  •  $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                )
                if (reminder.recurrence != "none") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = OrangePrimary.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = reminder.recurrence.toRecurrenceLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Ações
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(18.dp),
                        tint = OrangePrimary.copy(alpha = 0.75f),
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Excluir",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir lembrete?") },
            text = { Text("O lembrete \"${reminder.title}\" será removido permanentemente.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun ReminderGroup.label(): String = when (this) {
    ReminderGroup.HOJE -> "Hoje"
    ReminderGroup.AMANHA -> "Amanhã"
    ReminderGroup.ESTA_SEMANA -> "Próximos"
    ReminderGroup.HISTORICO -> "Histórico"
}

@DrawableRes
fun String.toCategoryDrawable(): Int = when (this) {
    "vacina"       -> R.drawable.icone_vacina
    "consulta"     -> R.drawable.icone_consulta
    "banho"        -> R.drawable.icone_banho
    "medicacao"    -> R.drawable.icone_medicacao
    "alimentacao"  -> R.drawable.icone_alimentacao
    "vermifugo"    -> R.drawable.icone_vermifugo
    else           -> R.drawable.icone_personalizado
}

fun String.toRecurrenceLabel(): String = when (this) {
    "daily"   -> "Repete diariamente"
    "weekly"  -> "Repete semanalmente"
    "monthly" -> "Repete mensalmente"
    else      -> ""
}
