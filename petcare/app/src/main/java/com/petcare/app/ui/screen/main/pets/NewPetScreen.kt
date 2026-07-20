package com.petcare.app.ui.screen.main.pets

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.petcare.app.R
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.theme.spacing
import com.petcare.app.ui.viewmodel.NewPetViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─── Espécies disponíveis (SPEC §11) — sempre ícones próprios, nunca emoji ───

private data class SpeciesOption(
    val storageValue: String,
    val label: String,
    @DrawableRes val iconRes: Int,
)

private val SPECIES_OPTIONS = listOf(
    SpeciesOption("cachorro", "Cachorro", R.drawable.icone_especie_cachorro),
    SpeciesOption("gato", "Gato", R.drawable.icone_especie_gato),
    SpeciesOption("pássaro", "Pássaro", R.drawable.icone_especie_passaro),
    SpeciesOption("peixe", "Peixe", R.drawable.icone_especie_peixe),
    SpeciesOption("réptil", "Réptil", R.drawable.icone_especie_reptil),
    SpeciesOption("roedor", "Roedor", R.drawable.icone_especie_roedor),
    SpeciesOption("outro", "Outro", R.drawable.icone_especie_outro),
)

private enum class NewPetStep(val label: String) {
    BASIC("Informações Básicas"),
    MEDICAL("Informações Médicas"),
    EMERGENCY("Contatos de Emergência"),
}

private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

/**
 * Formulário "Novo Pet" (SPEC §11), aberto pelo "+" da aba Meus Pets. Layout
 * em 3 blocos navegáveis (Informações Básicas, Informações Médicas,
 * Contatos de Emergência) com transições suaves entre eles. Ao salvar, grava
 * um [Pet] real no Room.
 *
 * A foto de perfil (SPEC §11 — parte 2) é escolhida na galeria e depois
 * cortada em [PetPhotoEditorScreen] — uma rota separada do NavGraph, por
 * isso o caminho escolhido mora em [NewPetViewModel.photoPath] (mesma
 * instância, escopada à entrada "new_pet") em vez de `rememberSaveable`
 * local; ver `onNavigateToPhotoEditor` e `PetCareNavGraph`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPetScreen(
    viewModel: NewPetViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToPhotoEditor: (Uri) -> Unit = {},
) {
    var step by rememberSaveable { mutableStateOf(NewPetStep.BASIC.ordinal) }
    val currentStep = NewPetStep.entries[step]
    val photoPath by viewModel.photoPath.collectAsState()

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onNavigateToPhotoEditor(uri)
    }
    fun launchPhotoPicker() {
        pickPhotoLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    // ── Informações Básicas ──────────────────────────────────────────────────
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf<String?>(null) }
    var sex by rememberSaveable { mutableStateOf<String?>(null) }
    var breed by rememberSaveable { mutableStateOf("") }
    var birthDateIso by rememberSaveable { mutableStateOf("") }
    var weightText by rememberSaveable { mutableStateOf("") }

    // ── Informações Médicas ──────────────────────────────────────────────────
    var bloodType by rememberSaveable { mutableStateOf("") }
    var allergies by rememberSaveable { mutableStateOf("") }
    var chronicConditions by rememberSaveable { mutableStateOf("") }
    var isCastrated by rememberSaveable { mutableStateOf(false) }
    var microchip by rememberSaveable { mutableStateOf("") }

    // ── Contatos de Emergência + observações ────────────────────────────────
    var vetName by rememberSaveable { mutableStateOf("") }
    var vetPhone by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    // isSaving vem do ViewModel (StateFlow) — fonte única de verdade.
    // O valor nunca volta a false enquanto a tela estiver montada; onSaved()
    // navega imediatamente para fora, destruindo a tela antes de qualquer
    // segundo toque poder chegar ao botão Salvar.
    val isSaving         by viewModel.isSaving.collectAsState()
    var attemptedAdvance by remember { mutableStateOf(false) }

    // ── Validações (SPEC §11) ────────────────────────────────────────────────
    val nameError = attemptedAdvance && name.isBlank()
    val weightValue = weightText.trim().replace(',', '.').toDoubleOrNull()
    val weightError = attemptedAdvance && weightText.isNotBlank() && (weightValue == null || weightValue <= 0.0)
    val birthDateError = attemptedAdvance && birthDateIso.isNotBlank() && isIsoDateInFuture(birthDateIso)

    fun validateBasicsAndAdvance() {
        attemptedAdvance = true
        val hasError = name.isBlank() ||
            (weightText.isNotBlank() && (weightValue == null || weightValue <= 0.0)) ||
            (birthDateIso.isNotBlank() && isIsoDateInFuture(birthDateIso))
        if (!hasError) {
            attemptedAdvance = false
            step = NewPetStep.MEDICAL.ordinal
        }
    }

    fun handleBack() {
        when {
            isSaving -> Unit
            step > 0 -> step -= 1
            else -> onDismiss()
        }
    }
    BackHandler(onBack = ::handleBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            TopAppBar(
                title = { Text("Novo Pet") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack, enabled = !isSaving) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )

            StepIndicator(currentStep = currentStep)

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        val enter = slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { if (forward) it / 3 else -it / 3 },
                        ) + fadeIn(tween(260))
                        val exit = slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { if (forward) -it / 3 else it / 3 },
                        ) + fadeOut(tween(180))
                        enter togetherWith exit
                    },
                    label = "new_pet_step",
                ) { stepValue ->
                    when (stepValue) {
                        NewPetStep.BASIC -> BasicInfoBlock(
                            photoPath = photoPath,
                            onPickPhoto = { launchPhotoPicker() },
                            name = name,
                            onNameChange = { name = it },
                            nameError = nameError,
                            species = species,
                            onSpeciesChange = { species = it },
                            sex = sex,
                            onSexChange = { sex = it },
                            breed = breed,
                            onBreedChange = { breed = it },
                            birthDateIso = birthDateIso,
                            onRequestDatePicker = { showDatePicker = true },
                            birthDateError = birthDateError,
                            weightText = weightText,
                            onWeightChange = { weightText = it },
                            weightError = weightError,
                        )
                        NewPetStep.MEDICAL -> MedicalInfoBlock(
                            bloodType = bloodType,
                            onBloodTypeChange = { bloodType = it },
                            allergies = allergies,
                            onAllergiesChange = { allergies = it },
                            chronicConditions = chronicConditions,
                            onChronicConditionsChange = { chronicConditions = it },
                            isCastrated = isCastrated,
                            onCastratedChange = { isCastrated = it },
                            microchip = microchip,
                            onMicrochipChange = { microchip = it },
                        )
                        NewPetStep.EMERGENCY -> EmergencyContactBlock(
                            vetName = vetName,
                            onVetNameChange = { vetName = it },
                            vetPhone = vetPhone,
                            onVetPhoneChange = { vetPhone = it },
                            notes = notes,
                            onNotesChange = { notes = it },
                        )
                    }
                }
            }

            BottomActionRow(
                step = currentStep,
                isSaving = isSaving,
                onBack = { step -= 1 },
                onNext = { validateBasicsAndAdvance() },
                onAdvanceFromMedical = { step = NewPetStep.EMERGENCY.ordinal },
                onSave = {
                    val pet = Pet(
                        name = name.trim(),
                        species = species ?: "outro",
                        breed = breed.trim(),
                        sex = sex ?: "",
                        isCastrated = isCastrated,
                        birthDate = birthDateIso,
                        weightKg = weightValue ?: 0.0,
                        bloodType = bloodType.trim(),
                        allergies = allergies.trim(),
                        chronicConditions = chronicConditions.trim(),
                        microchip = microchip.trim(),
                        notes = notes.trim(),
                        vetName = vetName.trim(),
                        vetPhone = vetPhone.trim(),
                        photoPath = photoPath ?: "",
                    )
                    // Navega imediatamente após o insert — tela destruída antes
                    // de qualquer segundo toque poder chegar ao botão Salvar.
                    viewModel.savePet(pet, onSaved)
                },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = isoDateFormat.parse(birthDateIso)?.time,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthDateIso = isoDateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

}

private fun isIsoDateInFuture(iso: String): Boolean {
    val parsed = runCatching { isoDateFormat.parse(iso) }.getOrNull() ?: return false
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
    }.time
    return parsed.after(today)
}

// ─── Indicador de progresso dos 3 blocos ─────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: NewPetStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NewPetStep.entries.forEach { s ->
                val active = s.ordinal <= currentStep.ordinal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${currentStep.ordinal + 1}/3 · ${currentStep.label}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

// ─── Bloco 1: Informações Básicas ────────────────────────────────────────────

@Composable
private fun BasicInfoBlock(
    photoPath: String?,
    onPickPhoto: () -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: Boolean,
    species: String?,
    onSpeciesChange: (String) -> Unit,
    sex: String?,
    onSexChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    birthDateIso: String,
    onRequestDatePicker: () -> Unit,
    birthDateError: Boolean,
    weightText: String,
    onWeightChange: (String) -> Unit,
    weightError: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        // Foto de perfil (SPEC §11 — parte 2): toca para escolher da galeria
        // e cortar; até lá, mostra o avatar padrão do pacote.
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                    .clickable(onClick = onPickPhoto),
                contentAlignment = Alignment.Center,
            ) {
                if (photoPath.isNullOrBlank()) {
                    Image(
                        painter = painterResource(R.drawable.avatar_pet_padrao),
                        contentDescription = "Foto do pet",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(photoPath))
                            .build(),
                        contentDescription = "Foto do pet",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Text(
            text = if (photoPath.isNullOrBlank()) "Toque para adicionar uma foto" else "Toque para trocar a foto",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome*") },
            isError = nameError,
            supportingText = { if (nameError) Text("O nome é obrigatório") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Espécie", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            items(SPECIES_OPTIONS, key = { it.storageValue }) { option ->
                SpeciesChip(
                    option = option,
                    selected = species == option.storageValue,
                    onClick = { onSpeciesChange(option.storageValue) },
                )
            }
        }

        Text("Sexo", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            SexOptionChip(
                label = "Macho",
                icon = Icons.Rounded.Male,
                selected = sex == "Macho",
                onClick = { onSexChange("Macho") },
                modifier = Modifier.weight(1f),
            )
            SexOptionChip(
                label = "Fêmea",
                icon = Icons.Rounded.Female,
                selected = sex == "Fêmea",
                onClick = { onSexChange("Fêmea") },
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedTextField(
            value = breed,
            onValueChange = onBreedChange,
            label = { Text("Raça (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        val dateInteractionSource = remember { MutableInteractionSource() }
        val isDatePressed by dateInteractionSource.collectIsPressedAsState()
        LaunchedEffect(isDatePressed) { if (isDatePressed) onRequestDatePicker() }

        OutlinedTextField(
            value = if (birthDateIso.isBlank()) "" else displayDateFormat.format(isoDateFormat.parse(birthDateIso) ?: Date()),
            onValueChange = {},
            readOnly = true,
            label = { Text("Data de nascimento (opcional)") },
            isError = birthDateError,
            supportingText = { if (birthDateError) Text("A data não pode ser no futuro") },
            trailingIcon = {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = "Selecionar data")
            },
            interactionSource = dateInteractionSource,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = weightText,
            onValueChange = onWeightChange,
            label = { Text("Peso (kg, opcional)") },
            isError = weightError,
            supportingText = { if (weightError) Text("Informe um peso numérico maior que zero") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.xs))
    }
}

@Composable
private fun SpeciesChip(
    option: SpeciesOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(if (selected) OrangePrimary.copy(alpha = 0.14f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Image(
            painter = painterResource(option.iconRes),
            contentDescription = option.label,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SexOptionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (selected) OrangePrimary.copy(alpha = 0.14f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = if (selected) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// ─── Bloco 2: Informações Médicas ────────────────────────────────────────────

@Composable
private fun MedicalInfoBlock(
    bloodType: String,
    onBloodTypeChange: (String) -> Unit,
    allergies: String,
    onAllergiesChange: (String) -> Unit,
    chronicConditions: String,
    onChronicConditionsChange: (String) -> Unit,
    isCastrated: Boolean,
    onCastratedChange: (Boolean) -> Unit,
    microchip: String,
    onMicrochipChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        OutlinedTextField(
            value = bloodType,
            onValueChange = onBloodTypeChange,
            label = { Text("Tipo sanguíneo (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = allergies,
            onValueChange = onAllergiesChange,
            label = { Text("Alergias (opcional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = chronicConditions,
            onValueChange = onChronicConditionsChange,
            label = { Text("Condições crônicas (opcional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Castrado(a)", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isCastrated,
                onCheckedChange = onCastratedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = OrangePrimary),
            )
        }

        OutlinedTextField(
            value = microchip,
            onValueChange = onMicrochipChange,
            label = { Text("Microchip (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.xs))
    }
}

// ─── Bloco 3: Contatos de Emergência + observações ──────────────────────────

@Composable
private fun EmergencyContactBlock(
    vetName: String,
    onVetNameChange: (String) -> Unit,
    vetPhone: String,
    onVetPhoneChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Text("Veterinário", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = vetName,
            onValueChange = onVetNameChange,
            label = { Text("Nome do veterinário (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = vetPhone,
            onValueChange = onVetPhoneChange,
            label = { Text("Telefone do veterinário (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Observações", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Observações gerais (opcional)") },
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.xs))
    }
}

// ─── Barra de ação inferior fixa (Voltar / Avançar / Salvar) ────────────────

@Composable
private fun BottomActionRow(
    step: NewPetStep,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onAdvanceFromMedical: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step != NewPetStep.BASIC) {
            TextButton(onClick = onBack, enabled = !isSaving) { Text("Voltar") }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        Button(
            onClick = {
                when (step) {
                    NewPetStep.BASIC -> onNext()
                    NewPetStep.MEDICAL -> onAdvanceFromMedical()
                    NewPetStep.EMERGENCY -> onSave()
                }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            modifier = Modifier.height(52.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text(
                    text = if (step == NewPetStep.EMERGENCY) "Salvar" else "Avançar",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─── Overlay de sucesso com bounce + partículas de pegadas (SPEC 16.5) ────────

