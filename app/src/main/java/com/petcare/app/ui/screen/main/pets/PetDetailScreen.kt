package com.petcare.app.ui.screen.main.pets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.petcare.app.R
import com.petcare.app.data.db.entity.HealthRecord
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.PetDetailViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─── Sub-abas de saúde do pet — Seção 12, Parte 1 (Vacinas, Medicamentos, Consultas) ──────────

private enum class HealthTab(val label: String) {
    VACCINES("Vacinas"),
    MEDICATIONS("Medicamentos"),
    CONSULTATIONS("Consultas"),
}

// ─── Formatação de data ───────────────────────────────────────────────────────

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
private fun Long.toDisplayDate(): String = dateFormat.format(this)
private fun Long?.toDisplayDateOrBlank(): String = if (this != null && this > 0L) dateFormat.format(this) else ""

// ─── Ponto de entrada da tela de detalhe do pet ──────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    viewModel: PetDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val pet by viewModel.pet.collectAsState()
    val vaccines by viewModel.vaccines.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val consultations by viewModel.consultations.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentTab = HealthTab.entries[selectedTabIndex]

    // Controle do BottomSheet de adicionar registro
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        topBar = {
            PetDetailTopBar(pet = pet, onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OrangePrimary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp,
                ),
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Adicionar registro",
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ── TabRow das sub-abas ──────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OrangePrimary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = OrangePrimary,
                    )
                },
            ) {
                HealthTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) OrangePrimary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
                            )
                        },
                    )
                }
            }

            // ── Conteúdo da aba selecionada ──────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    HealthTab.VACCINES ->
                        VacinasContent(
                            records = vaccines,
                            onDelete = { viewModel.deleteRecord(it) },
                        )
                    HealthTab.MEDICATIONS ->
                        MedicamentosContent(
                            records = medications,
                            onDelete = { viewModel.deleteRecord(it) },
                        )
                    HealthTab.CONSULTATIONS ->
                        ConsultasContent(
                            records = consultations,
                            onDelete = { viewModel.deleteRecord(it) },
                        )
                }
            }
        }
    }

    // ── BottomSheet de novo registro ─────────────────────────────────────────
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            when (currentTab) {
                HealthTab.VACCINES ->
                    AddVaccineForm(
                        petId = viewModel.petId,
                        onSave = { record ->
                            viewModel.insertRecord(record)
                            showAddSheet = false
                        },
                        onDismiss = { showAddSheet = false },
                    )
                HealthTab.MEDICATIONS ->
                    AddMedicationForm(
                        petId = viewModel.petId,
                        onSave = { record ->
                            viewModel.insertRecord(record)
                            showAddSheet = false
                        },
                        onDismiss = { showAddSheet = false },
                    )
                HealthTab.CONSULTATIONS ->
                    AddConsultationForm(
                        petId = viewModel.petId,
                        onSave = { record ->
                            viewModel.insertRecord(record)
                            showAddSheet = false
                        },
                        onDismiss = { showAddSheet = false },
                    )
            }
        }
    }
}

// ─── Cabeçalho da tela de detalhe ────────────────────────────────────────────

@Composable
private fun PetDetailTopBar(pet: Pet?, onBack: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)))
            .systemBarsPadding()
            .padding(horizontal = 4.dp, vertical = 10.dp),
    ) {
        // Botão voltar
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
            )
        }

        // Pet: foto + nome centralizado
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (pet?.photoPath?.isNotEmpty() == true) File(pet.photoPath) else null)
                    .crossfade(true)
                    .build(),
                contentDescription = pet?.name,
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.avatar_pet_padrao),
                error = painterResource(R.drawable.avatar_pet_padrao),
                placeholder = painterResource(R.drawable.avatar_pet_padrao),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )
            Text(
                text = pet?.name ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SUB-ABA: VACINAS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun VacinasContent(
    records: List<HealthRecord>,
    onDelete: (HealthRecord) -> Unit,
) {
    if (records.isEmpty()) {
        HealthEmptyState(
            imageRes = R.drawable.vazio_vacinas,
            title = "Nenhuma vacina registrada",
            message = "Adicione a primeira vacina do seu pet para acompanhar o histórico de imunização.",
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(records, key = { _, r -> r.id }) { index, record ->
                StaggeredHealthItem(index = index) {
                    VaccineCard(record = record, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
private fun VaccineCard(record: HealthRecord, onDelete: (HealthRecord) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    HealthRecordCard(
        iconRes = R.drawable.icone_vacina,
        onDeleteClick = { showDeleteDialog = true },
    ) {
        Text(
            text = record.vaccineName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val dateAndLot = buildString {
            append(record.dateMillis.toDisplayDate())
            if (record.vaccineLot.isNotBlank()) append(" · Lote: ${record.vaccineLot}")
        }
        Text(
            text = dateAndLot,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        if (record.nextDoseDate.isNotBlank()) {
            Text(
                text = "Próxima dose: ${record.nextDoseDate}",
                style = MaterialTheme.typography.bodySmall,
                color = OrangePrimary,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            message = "Remover a vacina \"${record.vaccineName}\"? Essa ação não pode ser desfeita.",
            onConfirm = {
                onDelete(record)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SUB-ABA: MEDICAMENTOS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun MedicamentosContent(
    records: List<HealthRecord>,
    onDelete: (HealthRecord) -> Unit,
) {
    if (records.isEmpty()) {
        HealthEmptyState(
            imageRes = R.drawable.vazio_medicamentos,
            title = "Nenhum medicamento registrado",
            message = "Registre os medicamentos do seu pet para acompanhar os tratamentos em andamento.",
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(records, key = { _, r -> r.id }) { index, record ->
                StaggeredHealthItem(index = index) {
                    MedicationCard(record = record, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(record: HealthRecord, onDelete: (HealthRecord) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    HealthRecordCard(
        iconRes = R.drawable.icone_medicacao,
        onDeleteClick = { showDeleteDialog = true },
    ) {
        Text(
            text = record.medicationName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val dosageFreq = buildString {
            if (record.medicationDosage.isNotBlank()) append(record.medicationDosage)
            if (record.medicationFrequency.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(record.medicationFrequency)
            }
        }
        if (dosageFreq.isNotBlank()) {
            Text(
                text = dosageFreq,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
        val durationAndDate = buildString {
            if (record.medicationDurationDays > 0) append("Duração: ${record.medicationDurationDays} dia(s)")
            append(if (isNotEmpty()) " · " else "")
            append(record.dateMillis.toDisplayDate())
        }
        Text(
            text = durationAndDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            message = "Remover o medicamento \"${record.medicationName}\"? Essa ação não pode ser desfeita.",
            onConfirm = {
                onDelete(record)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SUB-ABA: CONSULTAS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ConsultasContent(
    records: List<HealthRecord>,
    onDelete: (HealthRecord) -> Unit,
) {
    if (records.isEmpty()) {
        HealthEmptyState(
            imageRes = R.drawable.vazio_consultas,
            title = "Nenhuma consulta registrada",
            message = "Registre as consultas veterinárias do seu pet para manter o histórico de saúde completo.",
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(records, key = { _, r -> r.id }) { index, record ->
                StaggeredHealthItem(index = index) {
                    ConsultationCard(record = record, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
private fun ConsultationCard(record: HealthRecord, onDelete: (HealthRecord) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    HealthRecordCard(
        iconRes = R.drawable.icone_consulta,
        onDeleteClick = { showDeleteDialog = true },
    ) {
        Text(
            text = record.consultationReason.ifBlank { "Consulta" },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = record.dateMillis.toDisplayDate(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
        )
        if (record.diagnosis.isNotBlank()) {
            Text(
                text = "Diagnóstico: ${record.diagnosis}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (record.vetInstructions.isNotBlank()) {
            Text(
                text = "Orientações: ${record.vetInstructions}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            message = "Remover esta consulta de \"${record.dateMillis.toDisplayDate()}\"? Essa ação não pode ser desfeita.",
            onConfirm = {
                onDelete(record)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// COMPONENTES COMPARTILHADOS
// ════════════════════════════════════════════════════════════════════════════

// ─── Card base de registro de saúde ─────────────────────────────────────────

@Composable
private fun HealthRecordCard(
    iconRes: Int,
    onDeleteClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Ícone da categoria
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 2.dp),
            )

            // Conteúdo textual
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                content()
            }

            // Botão deletar
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Remover",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─── Animação de entrada escalonada (mesmo padrão da aba Meus Pets) ──────────

@Composable
private fun StaggeredHealthItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember(index) { mutableStateOf(false) }

    LaunchedEffect(index) {
        delay((index * 55L).coerceAtMost(380L))
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) +
            slideInVertically(
                animationSpec = tween(260),
                initialOffsetY = { it / 5 },
            ),
    ) {
        content()
    }
}

// ─── Estado vazio genérico das sub-abas ──────────────────────────────────────

@Composable
private fun HealthEmptyState(
    imageRes: Int,
    title: String,
    message: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.58f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Diálogo de confirmação de exclusão ──────────────────────────────────────

@Composable
private fun DeleteConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirmar exclusão", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text("Remover", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

// ════════════════════════════════════════════════════════════════════════════
// FORMULÁRIOS DE NOVO REGISTRO (BottomSheet)
// ════════════════════════════════════════════════════════════════════════════

// ─── Formulário: Nova Vacina ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVaccineForm(
    petId: Long,
    onSave: (HealthRecord) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var lot by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var nextDoseText by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNextDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateMillis,
    )
    val nextDatePickerState = rememberDatePickerState()

    val dateStr = dateMillis.toDisplayDate()
    val isValid = name.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.78f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Título do form
        Text(
            text = "Nova Vacina",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))

        // Nome da vacina
        HealthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome da vacina *",
            placeholder = "Ex: V10, Antirrábica",
        )

        // Data de aplicação
        HealthDateField(
            label = "Data de aplicação *",
            dateStr = dateStr,
            onClick = { showDatePicker = true },
        )

        // Lote (opcional)
        HealthTextField(
            value = lot,
            onValueChange = { lot = it },
            label = "Lote (opcional)",
            placeholder = "Ex: AB1234",
        )

        // Data da próxima dose (opcional)
        HealthDateField(
            label = "Data da próxima dose (opcional)",
            dateStr = nextDoseText,
            placeholder = "Selecionar data",
            onClick = { showNextDatePicker = true },
        )

        Spacer(Modifier.height(8.dp))

        // Botão Salvar
        Button(
            onClick = {
                onSave(
                    HealthRecord(
                        petId = petId,
                        type = "vaccine",
                        vaccineName = name.trim(),
                        vaccineLot = lot.trim(),
                        nextDoseDate = nextDoseText,
                        dateMillis = dateMillis,
                    )
                )
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
        ) {
            Text("Salvar vacina", fontWeight = FontWeight.Bold, color = Color.White)
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }

    // DatePicker — data de aplicação
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
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

    // DatePicker — próxima dose
    if (showNextDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showNextDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    nextDatePickerState.selectedDateMillis?.let {
                        nextDoseText = it.toDisplayDate()
                    }
                    showNextDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showNextDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = nextDatePickerState)
        }
    }
}

// ─── Formulário: Novo Medicamento ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicationForm(
    petId: Long,
    onSave: (HealthRecord) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    val dateStr = dateMillis.toDisplayDate()
    val isValid = name.isNotBlank() && dosage.isNotBlank() && frequency.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.82f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Novo Medicamento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))

        HealthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome do medicamento *",
            placeholder = "Ex: Bravecto, Frontline",
        )

        HealthTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = "Dosagem *",
            placeholder = "Ex: 5mg, 2 gotas, 1 comprimido",
        )

        HealthTextField(
            value = frequency,
            onValueChange = { frequency = it },
            label = "Frequência *",
            placeholder = "Ex: a cada 8h, 1x ao dia",
        )

        HealthTextField(
            value = durationText,
            onValueChange = { durationText = it.filter { c -> c.isDigit() } },
            label = "Duração em dias (opcional)",
            placeholder = "Ex: 7",
            keyboardType = KeyboardType.Number,
        )

        HealthDateField(
            label = "Data de início",
            dateStr = dateStr,
            onClick = { showDatePicker = true },
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                onSave(
                    HealthRecord(
                        petId = petId,
                        type = "medication",
                        medicationName = name.trim(),
                        medicationDosage = dosage.trim(),
                        medicationFrequency = frequency.trim(),
                        medicationDurationDays = durationText.toIntOrNull() ?: 0,
                        dateMillis = dateMillis,
                    )
                )
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
        ) {
            Text("Salvar medicamento", fontWeight = FontWeight.Bold, color = Color.White)
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
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
}

// ─── Formulário: Nova Consulta ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddConsultationForm(
    petId: Long,
    onSave: (HealthRecord) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    val dateStr = dateMillis.toDisplayDate()
    val isValid = reason.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.82f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Nova Consulta",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))

        HealthDateField(
            label = "Data da consulta",
            dateStr = dateStr,
            onClick = { showDatePicker = true },
        )

        HealthTextField(
            value = reason,
            onValueChange = { reason = it },
            label = "Motivo da visita *",
            placeholder = "Ex: Check-up, vacinação, mal-estar",
        )

        HealthTextField(
            value = diagnosis,
            onValueChange = { diagnosis = it },
            label = "Diagnóstico (opcional)",
            placeholder = "Ex: Saudável, Otite, Dermatite",
            singleLine = false,
            minLines = 2,
        )

        HealthTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = "Orientações do veterinário (opcional)",
            placeholder = "Ex: Retorno em 30 dias, evitar banho por 3 dias",
            singleLine = false,
            minLines = 2,
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                onSave(
                    HealthRecord(
                        petId = petId,
                        type = "consultation",
                        consultationReason = reason.trim(),
                        diagnosis = diagnosis.trim(),
                        vetInstructions = instructions.trim(),
                        dateMillis = dateMillis,
                    )
                )
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
        ) {
            Text("Salvar consulta", fontWeight = FontWeight.Bold, color = Color.White)
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
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
}

// ─── TextField genérico com o estilo do app ───────────────────────────────────

@Composable
private fun HealthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangePrimary,
            focusedLabelColor = OrangePrimary,
            cursorColor = OrangePrimary,
        ),
    )
}

// ─── Campo de data clicável ───────────────────────────────────────────────────
// Usa readOnly=true + MutableInteractionSource para detectar o press e abrir
// o DatePicker. Não usar enabled=false pois bloqueia eventos de pointer.

@Composable
private fun HealthDateField(
    label: String,
    dateStr: String,
    placeholder: String = "",
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) onClick()
    }

    OutlinedTextField(
        value = dateStr,
        onValueChange = {},
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        placeholder = { Text(placeholder.ifBlank { "Selecionar data" }, style = MaterialTheme.typography.bodyMedium) },
        readOnly = true,
        interactionSource = interactionSource,
        trailingIcon = {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = "Selecionar data",
                tint = OrangePrimary,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangePrimary,
            focusedLabelColor = OrangePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = OrangePrimary,
        ),
    )
}
