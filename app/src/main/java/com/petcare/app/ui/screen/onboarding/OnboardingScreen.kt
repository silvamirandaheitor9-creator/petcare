package com.petcare.app.ui.screen.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.R
import com.petcare.app.ui.screen.onboarding.components.FootprintIndicator
import com.petcare.app.ui.theme.BackgroundDark
import com.petcare.app.ui.theme.BackgroundLight
import com.petcare.app.ui.theme.OrangeDark
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// ─── Dados das 7 páginas ──────────────────────────────────────────────────────

private fun buildPages(): List<OnboardingPageData> = listOf(
    OnboardingPageData(
        imageRes = R.drawable.onboarding_1_boasvindas,
        title = "Bem-vindo ao PetCare!",
        subtitle = "Cuide dos seus pets com carinho — saúde, lembretes e memórias, tudo em um só lugar.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.onboarding_2_meuspets,
        title = "Seus Pets em Um Só Lugar",
        subtitle = "Cadastre cães, gatos e muito mais. Mantenha o histórico de saúde sempre à mão.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.onboarding_4_fotos,
        title = "Diário de Memórias",
        subtitle = "Registre os momentos especiais com fotos e edição criativa para nunca esquecer.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.onboarding_3_lembretes,
        title = "Nunca Perca um Cuidado",
        subtitle = "Lembretes de vacinas, consultas e remédios com notificações no horário certo.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.mel_avatar,
        title = "Conheça a Mel",
        subtitle = "A assistente do PetCare, sempre pronta para tirar suas dúvidas sobre os pets.",
    ),
    OnboardingPageData(
        imageRes = null,
        title = "Escolha o Seu Estilo",
        subtitle = "Você pode mudar quando quiser na aba Perfil.",
        isThemePage = true,
    ),
    OnboardingPageData(
        imageRes = null,
        title = "Antes de Começar",
        subtitle = "",
        isTermsPage = true,
    ),
)

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages = remember { buildPages() }
    val totalPages = pages.size
    val termsIndex = totalPages - 1

    val pagerState = rememberPagerState(pageCount = { totalPages })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isTermsPage = currentPage == termsIndex

    val selectedDark by viewModel.selectedDark.collectAsState()
    // (e) Botão "Aceitar e continuar" fica desabilitado até termsChecked ser true
    val termsChecked by viewModel.termsChecked.collectAsState()

    BackHandler(enabled = currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // ── Linha superior: botão Pular ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (!isTermsPage) {
                TextButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(termsIndex) } },
                ) {
                    Text(
                        text = "Pular",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrangePrimary,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // ── Pager com slide horizontal + fade ────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            beyondViewportPageCount = 1,
        ) { page ->
            val offset =
                ((pagerState.currentPage - page).toFloat() + pagerState.currentPageOffsetFraction)
                    .absoluteValue
            val alpha = (1f - offset * 0.55f).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha },
            ) {
                when {
                    pages[page].isThemePage -> ThemeSelectionPage(
                        data = pages[page],
                        selectedDark = selectedDark,
                        onSelect = { viewModel.selectTheme(it) },
                    )
                    // (a)(b)(c)(d)(e) — delegado a TermsPage.kt
                    pages[page].isTermsPage -> TermsPage(
                        isActive = pagerState.currentPage == page,
                        checked = termsChecked,
                        onCheckedChange = { viewModel.setTermsChecked(it) },
                    )
                    else -> StandardOnboardingPage(data = pages[page])
                }
            }
        }

        // ── Controles inferiores: pegadas + botão Próximo/Aceitar ────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FootprintIndicator(
                pageCount = totalPages,
                currentPage = currentPage,
            )
            Spacer(Modifier.height(20.dp))
            NextButton(
                label = if (isTermsPage) "Aceitar e continuar" else "Próximo",
                // (e) Desabilitado na tela de Termos até checkbox ser marcado
                enabled = if (isTermsPage) termsChecked else true,
                onClick = {
                    if (currentPage < totalPages - 1) {
                        scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                    } else {
                        viewModel.completeOnboarding()
                        onFinished()
                    }
                },
            )
        }
    }
}

// ─── Layout padrão (telas 1–5) ───────────────────────────────────────────────

@Composable
private fun StandardOnboardingPage(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        data.imageRes?.let { res ->
            Image(
                painter = painterResource(id = res),
                contentDescription = data.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = data.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Tela 6 — Seletor de tema ─────────────────────────────────────────────────

@Composable
private fun ThemeSelectionPage(
    data: OnboardingPageData,
    selectedDark: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = data.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThemeCard(
                icon = Icons.Outlined.LightMode,
                label = "Claro",
                previewBg = BackgroundLight,
                previewAccent = OrangePrimary,
                isSelected = !selectedDark,
                onClick = { onSelect(false) },
                modifier = Modifier.weight(1f),
            )
            ThemeCard(
                icon = Icons.Outlined.DarkMode,
                label = "Escuro",
                previewBg = BackgroundDark,
                previewAccent = OrangeDark,
                isSelected = selectedDark,
                onClick = { onSelect(true) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeCard(
    icon: ImageVector,
    label: String,
    previewBg: Color,
    previewAccent: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "theme_scale_$label",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else Color(0xFFD9C5BA),
        animationSpec = tween(durationMillis = 220),
        label = "theme_border_$label",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else Color(0xFFBDAFAA),
        animationSpec = tween(durationMillis = 220),
        label = "theme_icon_$label",
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                tint = iconTint,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(previewBg),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(previewAccent),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.80f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(previewAccent.copy(alpha = 0.35f)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(previewAccent.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}

// ─── Botão Próximo / Aceitar e continuar ─────────────────────────────────────

@Composable
private fun NextButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "next_btn_scale",
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth(0.72f)
            .height(52.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangePrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
