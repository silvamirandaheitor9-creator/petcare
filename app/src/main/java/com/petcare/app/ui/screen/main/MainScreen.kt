package com.petcare.app.ui.screen.main

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.HomeViewModel
import com.petcare.app.ui.viewmodel.PET_LIMIT_FREE
import com.petcare.app.ui.viewmodel.PetsViewModel
import java.util.Calendar

// ─── Definição das 5 abas ────────────────────────────────────────────────────

private enum class MainTab(
    val label: String,
    val icon: ImageVector,
    /** Quando true, exibe o FAB "+" empilhado abaixo do FAB do Mel. */
    val hasAddFab: Boolean,
) {
    HOME(label = "Início",         icon = Icons.Rounded.Home,        hasAddFab = false),
    PETS(label = "Meus Pets",      icon = Icons.Rounded.Pets,        hasAddFab = true),
    DIARY(label = "Diário",        icon = Icons.Rounded.AutoStories,  hasAddFab = true),
    REMINDERS(label = "Lembretes", icon = Icons.Rounded.Alarm,       hasAddFab = true),
    PROFILE(label = "Perfil",      icon = Icons.Rounded.Person,      hasAddFab = false),
}

// ─── Tela principal ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDiaryPhotoEditor: (Uri) -> Unit = {},
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentTab = MainTab.entries[selectedTabIndex]

    // ── Saudação contextual para a aba Início ────────────────────────────────
    // HomeViewModel é injetado aqui para obter o nome do usuário;
    // a mesma instância é repassada ao HomeScreen para evitar duplicação.
    val homeViewModel: HomeViewModel = hiltViewModel()
    val userName by homeViewModel.userName.collectAsState()

    // ── Badge "X/10" da aba Meus Pets (SPEC 8.2) ─────────────────────────────
    // PetsViewModel é injetado aqui (e repassado ao PetsScreen) para que o
    // contador fique disponível ao PetCareTopBar sem duplicar a instância.
    val petsViewModel: PetsViewModel = hiltViewModel()
    val petCount by petsViewModel.petCount.collectAsState()

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(userName, hour) {
        val name = userName.trim()
        when {
            hour < 12 -> if (name.isNotBlank()) "Bom dia, $name! ☀️" else "Bom dia! ☀️"
            hour < 18 -> if (name.isNotBlank()) "Boa tarde, $name! 🌤️" else "Boa tarde! 🌤️"
            else      -> if (name.isNotBlank()) "Boa noite, $name! 🌙" else "Boa noite! 🌙"
        }
    }
    val warmPhrase = remember(hour) {
        when {
            hour < 12 -> "Como estão os pets hoje?"
            hour < 18 -> "Tudo bem com os seus bichinhos?"
            else      -> "Como foi o dia dos seus pets?"
        }
    }

    // Mel bottom sheet (seção 15 implementará a lógica real de IA)
    var showMelSheet by remember { mutableStateOf(false) }
    val melSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Placeholder "Nova entrada" do Diário — o editor de fotos real chega numa
    // tarefa futura (SPEC 9.8-9.11); por ora o "+" só abre esta tela simples.
    var showAddDiaryEntry by remember { mutableStateOf(false) }

    Scaffold(
        // (SPEC §7) Aba Início: greeting personalizado com horário.
        // Demais abas: título padrão da aba.
        topBar = {
            PetCareTopBar(
                title    = if (selectedTabIndex == 0) greeting else currentTab.label,
                subtitle = if (selectedTabIndex == 0) warmPhrase else null,
                badge    = if (currentTab == MainTab.PETS) "$petCount/$PET_LIMIT_FREE" else null,
            )
        },
        bottomBar = {
            PetCareBottomBar(
                tabs = MainTab.entries,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ── Conteúdo da aba ───────────────────────────────────────────────
            when (currentTab) {
                MainTab.HOME ->
                    // (SPEC §7) Conteúdo real da aba Início
                    HomeScreen(
                        viewModel = homeViewModel,
                        onAddPet  = { selectedTabIndex = MainTab.PETS.ordinal },
                    )
                MainTab.PETS ->
                    // (SPEC §8) Conteúdo real da aba Meus Pets
                    PetsScreen(viewModel = petsViewModel)
                MainTab.DIARY ->
                    // (SPEC §9 — parte 1) Conteúdo real da aba Diário
                    DiaryScreen(
                        showAddEntryPlaceholder = showAddDiaryEntry,
                        onDismissAddEntryPlaceholder = { showAddDiaryEntry = false },
                        onNavigateToPhotoEditor = onNavigateToDiaryPhotoEditor,
                    )
                else ->
                    // Placeholder para as demais abas (seções 10, 14)
                    TabPlaceholder(tab = currentTab)
            }

            // ── FAB: pilha vertical no canto inferior direito ─────────────────
            // (SPEC 6): Mel acima do FAB de ação; ambos nunca se sobrepõem
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                // 1) Mel — sempre visível, sempre acima
                MelFab(onClick = { showMelSheet = true })

                // 2) "+" — apenas nas abas com ação de adicionar
                if (currentTab.hasAddFab) {
                    AddFab(
                        contentDescription = "Adicionar ${currentTab.label}",
                        onClick = {
                            when (currentTab) {
                                MainTab.DIARY -> showAddDiaryEntry = true
                                // Pets: aguarda o formulário "Novo Pet" (seção futura).
                                // Lembretes: aguarda a seção 10.
                                else -> { /* TODO: seções futuras */ }
                            }
                        },
                    )
                }
            }
        }
    }

    // ── Bottom sheet placeholder do Mel ──────────────────────────────────────
    if (showMelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMelSheet = false },
            sheetState = melSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.mel_avatar),
                    contentDescription = "Mel",
                    modifier = Modifier.size(88.dp),
                )
                Text(
                    text = "Mel — Assistente PetCare",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Oi! Em breve vou poder responder suas dúvidas sobre os seus pets. Aguarde! 🐾",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Cabeçalho: gradiente laranja + título + subtitle opcional ────────────────
// (SPEC §7): saudação personalizada com horário no header da aba Início.
// Para as demais abas: só title (sem subtitle), verticalmente centralizado.

@Composable
private fun PetCareTopBar(
    title: String,
    subtitle: String? = null,
    /** Selo opcional exibido ao lado do título — ex.: "3/10" na aba Meus Pets (SPEC 8.2). */
    badge: String? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                horizontal = 20.dp,
                vertical   = if (subtitle != null) 10.dp else 16.dp,
            ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
                if (!badge.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text  = badge,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
        }
    }
}

// ─── Barra de navegação inferior com animação "pulo" (SPEC 6.5) ──────────────

@Composable
private fun PetCareBottomBar(
    tabs: List<MainTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor   = OrangePrimary,
        tonalElevation = 4.dp,
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedIndex == index

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.22f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessHigh,
                ),
                label = "tab_jump_${tab.name}",
            )

            NavigationBarItem(
                selected = isSelected,
                onClick  = { onTabSelected(index) },
                icon     = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale),
                    )
                },
                label = {
                    Text(
                        text  = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = OrangePrimary,
                    selectedTextColor   = OrangePrimary,
                    indicatorColor      = OrangePrimary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                ),
            )
        }
    }
}

// ─── FAB do Mel ───────────────────────────────────────────────────────────────

@Composable
private fun MelFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick          = onClick,
        containerColor   = MaterialTheme.colorScheme.surface,
        contentColor     = OrangePrimary,
        elevation        = FloatingActionButtonDefaults.elevation(
            defaultElevation  = 6.dp,
            pressedElevation  = 2.dp,
        ),
        modifier = Modifier.size(56.dp),
    ) {
        Image(
            painter            = painterResource(R.drawable.mel_avatar_pequeno),
            contentDescription = "Mel — Assistente PetCare",
            modifier           = Modifier.size(40.dp),
        )
    }
}

// ─── FAB "+" de ação (Meus Pets, Diário, Lembretes) ─────────────────────────

@Composable
private fun AddFab(
    contentDescription: String,
    onClick: () -> Unit,
) {
    SmallFloatingActionButton(
        onClick        = onClick,
        containerColor = OrangePrimary,
        contentColor   = Color.White,
        elevation      = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
        ),
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            imageVector        = Icons.Rounded.Add,
            contentDescription = contentDescription,
            modifier           = Modifier.size(22.dp),
        )
    }
}

// ─── Placeholder para abas ainda não implementadas (seções 10, 14) ───────────

@Composable
private fun TabPlaceholder(tab: MainTab) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector        = tab.icon,
                contentDescription = null,
                modifier           = Modifier.size(56.dp),
                tint               = OrangePrimary.copy(alpha = 0.35f),
            )
            Text(
                text  = tab.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            )
        }
    }
}
