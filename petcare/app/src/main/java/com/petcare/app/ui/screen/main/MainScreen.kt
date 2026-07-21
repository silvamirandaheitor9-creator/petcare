package com.petcare.app.ui.screen.main

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.screen.main.pets.PetLimitSheet
import com.petcare.app.ui.viewmodel.HomeViewModel
import com.petcare.app.ui.viewmodel.PetsViewModel
import com.petcare.app.ui.viewmodel.ReminderViewModel
import java.util.Calendar

// ─── Definição das 5 abas ────────────────────────────────────────────────────

private enum class MainTab(
    val label: String,
    val icon: ImageVector,
    /** Quando true, exibe o FAB "+" de ação. */
    val hasAddFab: Boolean,
) {
    HOME     (label = "Início",    icon = Icons.Rounded.Favorite,      hasAddFab = false),
    PETS     (label = "Meus Pets", icon = Icons.Rounded.Pets,          hasAddFab = true),
    DIARY    (label = "Diário",    icon = Icons.Rounded.PhotoLibrary,   hasAddFab = true),
    REMINDERS(label = "Lembretes", icon = Icons.Rounded.Alarm,       hasAddFab = true),
    PROFILE  (label = "Perfil",    icon = Icons.Rounded.Person,      hasAddFab = false),
}

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun MainScreen(
    onNavigateToDiaryPhotoEditor: (Uri) -> Unit = {},
    onNavigateToNewPet: () -> Unit = {},
    onNavigateToEditPet: (petId: Long) -> Unit = {},
    onNavigateToNewReminder: (reminderId: Long) -> Unit = {},
    onNavigateToPetDetail: (petId: Long) -> Unit = {},
    onNavigateToEditDiaryEntry: (entryId: Long) -> Unit = {},
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentTab = MainTab.entries[selectedTabIndex]

    // ── Saudação contextual para a aba Início ────────────────────────────────
    val homeViewModel: HomeViewModel = hiltViewModel()
    val userName by homeViewModel.userName.collectAsState()

    // ── Badge "X/N" da aba Meus Pets (SPEC 8.2 + §18) ───────────────────────
    val petsViewModel: PetsViewModel = hiltViewModel()
    val petCount by petsViewModel.petCount.collectAsState()
    val petLimit by petsViewModel.petLimit.collectAsState()

    // ── Controle do sheet de limite de pets (SPEC §18.3-18.4) ────────────────
    var showPetLimitSheet by remember { mutableStateOf(false) }

    // ── ViewModel de Lembretes ────────────────────────────────────────────────
    val reminderViewModel: ReminderViewModel = hiltViewModel()

    // ── Trigger do seletor de galeria da aba Diário ───────────────────────────
    var showAddDiaryEntry by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            PetCareTopBar(
                title    = if (selectedTabIndex == 0) greeting else currentTab.label,
                subtitle = if (selectedTabIndex == 0) warmPhrase else null,
                badge    = if (currentTab == MainTab.PETS) "$petCount/$petLimit" else null,
            )
        },
        bottomBar = {
            Column {
                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 12.dp),
                )
                PetCareBottomBar(
                    tabs          = MainTab.entries,
                    selectedIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ── Conteúdo da aba com crossfade suave ao trocar ─────────────────
            AnimatedContent(
                targetState   = currentTab,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(140))
                },
                label = "tab_content",
                modifier = Modifier.fillMaxSize(),
            ) { tab ->
                when (tab) {
                    MainTab.HOME ->
                        HomeScreen(
                            viewModel = homeViewModel,
                            onAddPet  = { selectedTabIndex = MainTab.PETS.ordinal },
                        )
                    MainTab.PETS ->
                        PetsScreen(
                            viewModel  = petsViewModel,
                            onPetClick = onNavigateToPetDetail,
                            onEditPet  = onNavigateToEditPet,
                        )
                    MainTab.DIARY ->
                        DiaryScreen(
                            showAddEntryPlaceholder      = showAddDiaryEntry,
                            onDismissAddEntryPlaceholder = { showAddDiaryEntry = false },
                            onNavigateToPhotoEditor      = onNavigateToDiaryPhotoEditor,
                            onEditEntry                  = onNavigateToEditDiaryEntry,
                        )
                    MainTab.REMINDERS ->
                        RemindersScreen(
                            viewModel               = reminderViewModel,
                            onNavigateToNewReminder = { id -> onNavigateToNewReminder(id) },
                        )
                    MainTab.PROFILE ->
                        ProfileScreen()
                }
            }

            // ── FAB "+" de ação (apenas abas com ação de adicionar) ──────────
            if (currentTab.hasAddFab) {
                AddFab(
                    contentDescription = "Adicionar ${currentTab.label}",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    onClick = {
                        when (currentTab) {
                            MainTab.PETS -> {
                                if (petCount >= petLimit) showPetLimitSheet = true
                                else onNavigateToNewPet()
                            }
                            MainTab.DIARY      -> showAddDiaryEntry = true
                            MainTab.REMINDERS  -> onNavigateToNewReminder(-1L)
                            else -> {}
                        }
                    },
                )
            }
        }
    }

    // ── Sheet de limite de pets com rewarded ad (SPEC §18.3-18.4) ───────────
    if (showPetLimitSheet) {
        PetLimitSheet(
            petCount   = petCount,
            petLimit   = petLimit,
            onUnlocked = { petsViewModel.unlockExtraSlots() },
            onDismiss  = { showPetLimitSheet = false },
        )
    }
}

// ─── Cabeçalho: gradiente laranja + título + subtitle opcional ────────────────

@Composable
private fun PetCareTopBar(
    title: String,
    subtitle: String? = null,
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
                verticalAlignment      = Alignment.CenterVertically,
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
                            text       = badge,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White,
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
                        imageVector        = tab.icon,
                        contentDescription = tab.label,
                        modifier           = Modifier
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

// ─── FAB "+" de ação com pulso suave (SPEC 16.4 adaptado) ────────────────────

@Composable
private fun AddFab(
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // Pulsação sutil: escala 1.0 → 1.10 → 1.0 em loop (respira como o Mel da spec)
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.10f,
        animationSpec = infiniteRepeatable(
            animation  = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fab_scale",
    )

    SmallFloatingActionButton(
        onClick        = onClick,
        containerColor = OrangePrimary,
        contentColor   = Color.White,
        elevation      = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
        ),
        modifier = modifier
            .size(48.dp)
            .scale(fabScale),
    ) {
        Icon(
            imageVector        = Icons.Rounded.Add,
            contentDescription = contentDescription,
            modifier           = Modifier.size(24.dp),
        )
    }
}

// ─── Banner AdMob (centralizado aqui para todas as abas) ─────────────────────

@Composable
private fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test banner ID
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier,
    )
}
