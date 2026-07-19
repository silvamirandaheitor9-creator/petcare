package com.petcare.app.ui.screen.main

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.ProfileUiEvent
import com.petcare.app.ui.viewmodel.ProfileViewModel
import com.petcare.app.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val context         = LocalContext.current
    val focusManager    = LocalFocusManager.current
    val scope           = rememberCoroutineScope()
    val snackbarState   = remember { SnackbarHostState() }

    // ── State ─────────────────────────────────────────────────────────────────
    val userName  by viewModel.userName.collectAsState()
    val isDark    by themeViewModel.isDarkTheme.collectAsState()

    var nameInput         by remember(userName) { mutableStateOf(userName) }
    var pendingImportUri  by remember { mutableStateOf<android.net.Uri?>(null) }

    // Dialog flags
    var showImportDialog  by remember { mutableStateOf(false) }
    var showDeleteDialog1 by remember { mutableStateOf(false) }
    var showDeleteDialog2 by remember { mutableStateOf(false) }

    // ── Eventos do ViewModel ──────────────────────────────────────────────────
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.ExportSuccess ->
                    snackbarState.showSnackbar("Prontinho! Seus dados estão salvos com segurança 🐾")
                is ProfileUiEvent.ExportError ->
                    snackbarState.showSnackbar("Erro ao exportar: ${event.msg}")
                is ProfileUiEvent.ImportSuccess ->
                    snackbarState.showSnackbar("Backup importado com sucesso! 🐾")
                is ProfileUiEvent.ImportError ->
                    snackbarState.showSnackbar("Erro ao importar: ${event.msg}")
                is ProfileUiEvent.DeleteSuccess ->
                    snackbarState.showSnackbar("Todos os dados foram apagados.")
            }
        }
    }

    // ── SAF: exportar (escolher pasta) ────────────────────────────────────────
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            // Permissão persistente para acessar a pasta futuramente
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            viewModel.exportBackup(context.contentResolver, treeUri)
        }
    }

    // ── SAF: importar (escolher arquivo) ─────────────────────────────────────
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        if (fileUri != null) {
            pendingImportUri  = fileUri
            showImportDialog  = true
        }
    }

    // ── Diálogo: mesclar ou substituir ───────────────────────────────────────
    if (showImportDialog && pendingImportUri != null) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Importar Backup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Como você quer importar os dados?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    OutlinedButton(
                        onClick = {
                            showImportDialog = false
                            viewModel.importBackup(context.contentResolver, pendingImportUri!!, merge = true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text("Mesclar com dados atuais")
                    }
                    Button(
                        onClick = {
                            showImportDialog = false
                            viewModel.importBackup(context.contentResolver, pendingImportUri!!, merge = false)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    ) {
                        Text("Substituir tudo")
                    }
                    TextButton(
                        onClick = { showImportDialog = false },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Cancelar") }
                }
            }
        }
    }

    // ── Diálogo: apagar dados — 1ª confirmação ────────────────────────────────
    if (showDeleteDialog1) {
        Dialog(onDismissRequest = { showDeleteDialog1 = false }) {
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Apagar todos os dados?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Todos os pets, lembretes, entradas do diário e registros de saúde serão removidos do aparelho.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(onClick = { showDeleteDialog1 = false }) { Text("Cancelar") }
                        Button(
                            onClick = { showDeleteDialog1 = false; showDeleteDialog2 = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(24.dp),
                        ) { Text("Continuar") }
                    }
                }
            }
        }
    }

    // ── Diálogo: apagar dados — 2ª confirmação (irreversível) ─────────────────
    if (showDeleteDialog2) {
        Dialog(onDismissRequest = { showDeleteDialog2 = false }) {
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Tem certeza absoluta?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        "Esta ação é irreversível e não pode ser desfeita. Considere exportar um backup antes de continuar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(onClick = { showDeleteDialog2 = false }) { Text("Cancelar") }
                        Button(
                            onClick = {
                                showDeleteDialog2 = false
                                viewModel.deleteAllData()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(24.dp),
                        ) { Text("Apagar tudo") }
                    }
                }
            }
        }
    }

    // ── Layout principal ──────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── 1. Nome do usuário ────────────────────────────────────────────
            item {
                ProfileCard(title = "Como podemos te chamar?", icon = Icons.Rounded.Person) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Seu apelido ou nome") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction      = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.setUserName(nameInput)
                            focusManager.clearFocus()
                            scope.launch {
                                snackbarState.showSnackbar("Nome salvo! 🐾")
                            }
                        }),
                        shape  = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = OrangePrimary,
                            focusedLabelColor    = OrangePrimary,
                            cursorColor          = OrangePrimary,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Este nome aparece na saudação da tela Início.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }

            // ── 2. Aparência (tema) ───────────────────────────────────────────
            item {
                ProfileCard(title = "Aparência", icon = if (isDark) Icons.Rounded.LightMode
                                                         else        Icons.Rounded.DarkMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            // Ícone: lua quando tema claro, sol quando tema escuro (SPEC 14)
                            val themeLabel = if (isDark) "Tema Escuro" else "Tema Claro"
                            Text(
                                themeLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                if (isDark) "Toque para usar o tema claro"
                                else        "Toque para usar o tema escuro",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }

                        // Switch com animação de cor suave (~300ms) SPEC 16
                        val trackColor by animateColorAsState(
                            targetValue = if (isDark) OrangePrimary else Color.LightGray,
                            animationSpec = tween(durationMillis = 300),
                            label = "theme_track",
                        )
                        Switch(
                            checked  = isDark,
                            onCheckedChange = { themeViewModel.setDarkTheme(it) },
                            thumbContent = {
                                Icon(
                                    imageVector = if (isDark) Icons.Rounded.LightMode
                                                  else        Icons.Rounded.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isDark) OrangePrimary else Color.Gray,
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor   = OrangePrimary.copy(alpha = 0.4f),
                                checkedThumbColor   = OrangePrimary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            ),
                        )
                    }
                }
            }

            // ── 3. Backup e Restauração ───────────────────────────────────────
            item {
                ProfileCard(title = "Backup e Restauração", icon = Icons.Rounded.Backup) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick   = { exportLauncher.launch(null) },
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(24.dp),
                            colors    = ButtonDefaults.outlinedButtonColors(
                                contentColor = OrangePrimary),
                        ) {
                            Icon(Icons.Rounded.Upload, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Exportar backup", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick   = { importLauncher.launch(arrayOf("*/*")) },
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(24.dp),
                            colors    = ButtonDefaults.outlinedButtonColors(
                                contentColor = OrangePrimary),
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Importar backup", fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "O backup salva todos os pets, lembretes, diário e registros de saúde num arquivo .db.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                }
            }

            // ── 4. Meus Dados (apagar) ────────────────────────────────────────
            item {
                ProfileCard(title = "Meus Dados", icon = Icons.Rounded.DeleteForever) {
                    TextButton(
                        onClick = { showDeleteDialog1 = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Apagar todos os dados",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // ── 5. Seções legais ──────────────────────────────────────────────
            item {
                ExpandableSection(
                    title  = "Política de Privacidade",
                    icon   = Icons.Rounded.Policy,
                ) {
                    LegalText(PRIVACY_POLICY_TEXT)
                }
            }
            item {
                ExpandableSection(
                    title = "Termos de Uso",
                    icon  = Icons.Rounded.Gavel,
                ) {
                    LegalText(TERMS_OF_USE_TEXT)
                }
            }
            item {
                ExpandableSection(
                    title = "Sobre o PetCare",
                    icon  = Icons.Rounded.Info,
                ) {
                    LegalText(ABOUT_TEXT)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // ── Snackbar flutuante ────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarState,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
            snackbar  = { data ->
                Snackbar(
                    snackbarData   = data,
                    shape          = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor   = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }
}

// ─── Card de seção de perfil ──────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    title   : String,
    icon    : ImageVector,
    content : @Composable () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Cabeçalho da card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, contentDescription = null,
                    tint     = OrangePrimary,
                    modifier = Modifier.size(20.dp))
                Text(
                    title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            content()
        }
    }
}

// ─── Seção expansível (legal) ─────────────────────────────────────────────────

@Composable
private fun ExpandableSection(
    title   : String,
    icon    : ImageVector,
    content : @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabeçalho clicável
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(if (expanded) RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                          else          RoundedCornerShape(16.dp))
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(icon, contentDescription = null,
                        tint     = OrangePrimary,
                        modifier = Modifier.size(20.dp))
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    imageVector        = if (expanded) Icons.Rounded.ExpandLess
                                        else           Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier           = Modifier.size(20.dp),
                )
            }

            // Conteúdo expansível com animação
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(animationSpec = tween(250)) + fadeIn(tween(250)),
                exit    = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(200)),
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

// ─── Texto legal formatado ────────────────────────────────────────────────────

@Composable
private fun LegalText(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        lineHeight = androidx.compose.ui.unit.TextUnit(
            20f, androidx.compose.ui.unit.TextUnitType.Sp),
    )
}

// ─── Textos legais (SPEC §19) ─────────────────────────────────────────────────

private const val PRIVACY_POLICY_TEXT = """Política de Privacidade do PetCare
Última atualização: Julho de 2026

O PetCare foi criado para funcionar sem exigir cadastro ou conta de usuário. Não coletamos nem armazenamos seus dados pessoais em nenhum servidor.

Dados armazenados no seu dispositivo: nomes, fotos, datas e informações de saúde dos seus pets, as entradas do Diário e os lembretes ficam salvos apenas no seu aparelho.

Permissões usadas pelo app: Câmera, Galeria e Notificações. Cada permissão só é solicitada no momento do uso.

Publicidade: o PetCare exibe anúncios fornecidos pelo Google AdMob. O AdMob pode coletar identificadores de publicidade do dispositivo.

Conformidade com a LGPD: seguimos os princípios da Lei Geral de Proteção de Dados. Você tem controle total sobre seus dados."""

private const val TERMS_OF_USE_TEXT = """Termos de Uso do PetCare
Última atualização: Julho de 2026

1. Natureza do app: ferramenta de organização pessoal para cuidados com pets.

2. Seus dados: armazenados localmente. Você é responsável pelos backups.

3. Anúncios: o app oferece recursos gratuitos com limite de pets, expansível via anúncios.

4. Propriedade intelectual: nome, mascote e design são de nossa propriedade."""

private const val ABOUT_TEXT = """Sobre o PetCare

PetCare é um aplicativo criado para ajudar tutores a cuidarem melhor dos seus pets — de forma simples, organizada e com carinho.

Versão: 1.0.0"""
