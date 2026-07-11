package com.petcare.app.ui.screen.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary

// ─── Definição das 5 abas ────────────────────────────────────────────────────

private enum class MainTab(
    val label: String,
    val icon: ImageVector,
    /** Quando true, exibe o FAB "+" empilhado abaixo do FAB do Mel. */
    val hasAddFab: Boolean,
) {
    HOME(label = "Início",     icon = Icons.Rounded.Home,        hasAddFab = false),
    PETS(label = "Meus Pets",  icon = Icons.Rounded.Pets,        hasAddFab = true),
    DIARY(label = "Diário",    icon = Icons.Rounded.AutoStories,  hasAddFab = true),
    REMINDERS(label = "Lembretes", icon = Icons.Rounded.Alarm,   hasAddFab = true),
    PROFILE(label = "Perfil",  icon = Icons.Rounded.Person,      hasAddFab = false),
}

// ─── Tela principal ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentTab = MainTab.entries[selectedTabIndex]

    // Mel bottom sheet (seção 15 implementará a lógica real)
    var showMelSheet by remember { mutableStateOf(false) }
    val melSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        // (SPEC) Cabeçalho: só texto sobre gradiente laranja — sem imagem do mascote
        topBar = { PetCareTopBar(title = currentTab.label) },
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
            // Conteúdo da aba — placeholder até as seções 7-14
            TabPlaceholder(tab = currentTab)

            // ── FAB: pilha vertical no canto inferior direito ─────────────────
            // (SPEC 6): "Quando uma tela tiver os dois botões, empilhe-os
            // verticalmente — o botão do Mel fica acima do botão de ação."
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                // 1) Mel — sempre visível em todas as abas, sempre acima
                MelFab(onClick = { showMelSheet = true })

                // 2) "+" — apenas nas abas com ação de adicionar (Pets, Diário, Lembretes)
                if (currentTab.hasAddFab) {
                    AddFab(
                        contentDescription = "Adicionar ${currentTab.label}",
                        onClick = { /* TODO seções 8-10: navegar para formulário */ },
                    )
                }
            }
        }
    }

    // ── Bottom sheet placeholder do Mel (seção 15 implementará a IA completa) ─
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

// ─── Cabeçalho: gradiente laranja + título (SPEC: nunca imagem pequena) ───────

@Composable
private fun PetCareTopBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(OrangeGradStart, OrangeGradEnd)),
            )
            // Aplica o inset da status bar — necessário pois usamos enableEdgeToEdge()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
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
        contentColor = OrangePrimary,
        tonalElevation = 4.dp,
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedIndex == index

            // (SPEC 6.5) Ícone selecionado faz "pulo" via spring com overshoot
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.22f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh,
                ),
                label = "tab_jump_${tab.name}",
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
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
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangePrimary,
                    selectedTextColor = OrangePrimary,
                    indicatorColor = OrangePrimary.copy(alpha = 0.12f),
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
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = OrangePrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
        ),
        modifier = Modifier.size(56.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.mel_avatar_pequeno),
            contentDescription = "Mel — Assistente PetCare",
            modifier = Modifier.size(40.dp),
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
        onClick = onClick,
        containerColor = OrangePrimary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
        ),
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ─── Conteúdo placeholder de cada aba ────────────────────────────────────────
// Será substituído aba por aba nas seções 7-14 do SPEC.

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
                imageVector = tab.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = OrangePrimary.copy(alpha = 0.35f),
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            )
        }
    }
}
