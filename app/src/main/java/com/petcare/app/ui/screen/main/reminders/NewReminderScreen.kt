package com.petcare.app.ui.screen.main.reminders

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.R
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.NewReminderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─── Tela "Novo Lembrete" / Editar Lembrete (SPEC §10 — redesign completo) ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReminderScreen(
    reminderId: Long = -1L,
    viewModel: NewReminderViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    val pets by viewModel.pets.collectAsState()

    LaunchedEffect(reminderId) {
        if (reminderId > 0L) viewModel.loadReminder(reminderId)
    }

    // Pré-seleciona o primeiro pet se ainda não há seleção e há pets disponíveis
    LaunchedEffect(pets) {
        if (viewModel.selectedPetId <= 0L && pets.isNotEmpty()) {
            viewModel.selectedPetId = pets.first().id
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(viewModel.dateTimeMillis)
    val timeStr = timeFormat.format(viewModel.dateTimeMillis)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            // ── Cabeçalho com gradiente (mesmo padrão das outras telas) ────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd))
                    )
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                    )
                }
                Text(
                    text = if (reminderId > 0L) "Editar Lembrete" else "Novo Lembrete",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            // ── Formulário ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                // Título
                FormSection(title = "Título *") {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: Vacina anual do Rex") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = outlinedFieldColors(),
                    )
                }

                // Pet
                if (pets.isNotEmpty()) {
                    FormSection(title = "Pet *") {
                        PetDropdown(
                            pets = pets,
                            selectedPetId = viewModel.selectedPetId,
                            onPetSelected = { viewModel.selectedPetId = it },
                        )
                    }
                }

                // Categoria
                FormSection(title = "Categoria") {
                    CategorySelector(
                        selected = viewModel.category,
                        onSelect = { viewModel.category = it },
                    )
                }

                // Data e Hora
                FormSection(title = "Data e Hora") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Botão Data
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp),
                                )
                                .clickable { showDatePicker = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        // Botão Hora
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp),
                                )
                                .clickable { showTimePicker = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                // Recorrência
                FormSection(title = "Repetir") {
                    RecurrenceSelector(
                        selected = viewModel.recurrence,
                        onSelect = { viewModel.recurrence = it },
                    )
                }

                // Observações
                FormSection(title = "Observações (opcional)") {
                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Notes,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Adicione detalhes relevantes...")
                            }
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = outlinedFieldColors(),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botão salvar
                Button(
                    onClick = { viewModel.saveReminder(onSaved) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = viewModel.isValid && !viewModel.isSaving,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = OrangePrimary.copy(alpha = 0.35f),
                        disabledContentColor = Color.White,
                    ),
                ) {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (reminderId > 0L) "Salvar alterações" else "Criar lembrete",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── DatePicker Dialog ─────────────────────────────────────────────────────
    if (showDatePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = viewModel.dateTimeMillis }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.dateTimeMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val selectedCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            // Preserva hora e minuto do valor atual
                            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        viewModel.dateTimeMillis = selectedCal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── TimePicker Dialog ─────────────────────────────────────────────────────
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = viewModel.dateTimeMillis }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        "Selecionar horário",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = viewModel.dateTimeMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.dateTimeMillis = newCal.timeInMillis
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

// ─── Seletor de categoria (grade de ícones) ───────────────────────────────────

private data class CategoryItem(val key: String, val label: String, @DrawableRes val icon: Int)

private val CATEGORIES = listOf(
    CategoryItem("vacina",      "Vacina",      R.drawable.icone_vacina),
    CategoryItem("consulta",    "Consulta",    R.drawable.icone_consulta),
    CategoryItem("banho",       "Banho",       R.drawable.icone_banho),
    CategoryItem("medicacao",   "Medicação",   R.drawable.icone_medicacao),
    CategoryItem("alimentacao", "Alimentação", R.drawable.icone_alimentacao),
    CategoryItem("vermifugo",   "Vermífugo",   R.drawable.icone_vermifugo),
    CategoryItem("personalizado","Personalizado",R.drawable.icone_personalizado),
)

@Composable
private fun CategorySelector(selected: String, onSelect: (String) -> Unit) {
    val chunked = CATEGORIES.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { cat ->
                    val isSelected = selected == cat.key
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) OrangePrimary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) OrangePrimary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelect(cat.key) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(
                            painter = painterResource(cat.icon),
                            contentDescription = cat.label,
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Text(
                            text = cat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) OrangePrimary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
                // Células vazias para completar a última linha
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Seletor de recorrência ───────────────────────────────────────────────────

private val RECURRENCES = listOf(
    "none"    to "Não repete",
    "daily"   to "Diariamente",
    "weekly"  to "Semanalmente",
    "monthly" to "Mensalmente",
)

@Composable
private fun RecurrenceSelector(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RECURRENCES.forEach { (key, label) ->
            val isSelected = selected == key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isSelected) OrangePrimary else OrangePrimary.copy(alpha = 0.07f)
                    )
                    .clickable { onSelect(key) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else OrangePrimary,
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ─── Dropdown de seleção de pet ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDropdown(
    pets: List<Pet>,
    selectedPetId: Long,
    onPetSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPet = pets.find { it.id == selectedPetId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedPet?.name ?: "Selecione um pet",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(16.dp),
            colors = outlinedFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            pets.forEach { pet ->
                DropdownMenuItem(
                    text = { Text(pet.name) },
                    onClick = {
                        onPetSelected(pet.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ─── Wrapper de seção do formulário ──────────────────────────────────────────

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        content()
    }
}

// ─── Cores padrão para OutlinedTextField ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = OrangePrimary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
    focusedLabelColor    = OrangePrimary,
    cursorColor          = OrangePrimary,
)
