package com.petcare.app.ui.screen.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.R
import com.petcare.app.ui.screen.onboarding.components.FootprintIndicator
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// ─── Dados das 7 páginas ──────────────────────────────────────────────────────

private fun buildPages(): List<OnboardingPageData> = listOf(
    // Tela 1 — Boas-vindas
    OnboardingPageData(
        imageRes = R.drawable.onboarding_1_boasvindas,
        title = "Bem-vindo ao PetCare!",
        subtitle = "Cuide dos seus pets com carinho — saúde, lembretes e memórias, tudo em um só lugar.",
    ),
    // Tela 2 — Meus Pets
    OnboardingPageData(
        imageRes = R.drawable.onboarding_2_meuspets,
        title = "Seus Pets em Um Só Lugar",
        subtitle = "Cadastre cães, gatos e muito mais. Mantenha o histórico de saúde sempre à mão.",
    ),
    // Tela 3 — Diário  (tarefa 2)
    OnboardingPageData(
        imageRes = R.drawable.onboarding_4_fotos,
        title = "Diário de Memórias",
        subtitle = "Registre os momentos especiais com fotos e edição criativa para nunca esquecer.",
    ),
    // Tela 4 — Lembretes  (tarefa 2)
    OnboardingPageData(
        imageRes = R.drawable.onboarding_3_lembretes,
        title = "Nunca Perca um Cuidado",
        subtitle = "Lembretes de vacinas, consultas e remédios com notificações no horário certo.",
    ),
    // Tela 5 — Assistente Mel  (tarefa 2)
    OnboardingPageData(
        imageRes = R.drawable.mel_avatar,
        title = "Conheça a Mel",
        subtitle = "A assistente do PetCare, sempre pronta para tirar suas dúvidas sobre os pets.",
    ),
    // Tela 6 — Escolha de tema  (tarefa 3)
    OnboardingPageData(
        imageRes = null,
        title = "Escolha o Seu Estilo",
        subtitle = "Tema claro ou escuro — você muda quando quiser na aba Perfil.",
        isThemePage = true,
    ),
    // Tela 7 — Termos e privacidade  (tarefa 4)
    OnboardingPageData(
        imageRes = null,
        title = "Antes de Começar",
        subtitle = "",
        isTermsPage = true,
    ),
)

// ─── Tela principal ───────────────────────────────────────────────────────────

/**
 * OnboardingScreen — SPEC seção 5, tarefa 1 de 4.
 *
 * Tarefa 1 implementa: pager de 7 páginas, indicador de pegadas, botões
 * Próximo/Pular, gesto de voltar. Telas 1 e 2 são completas; 3–7 são stubs
 * visuais que validam a mecânica de navegação.
 * Telas 3–5 concluídas na tarefa 2, tela 6 na tarefa 3, tela 7 na tarefa 4.
 */
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

    // Gesto de voltar do sistema: página anterior; fecha o app só na primeira página
    BackHandler(enabled = currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // ── Linha superior: espaço + botão Pular ─────────────────────────────
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
                // Mantém a altura da linha mesmo sem o botão
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
            // Fade proporcional ao deslocamento da página
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
                    pages[page].isThemePage -> ThemeStubPage()
                    pages[page].isTermsPage -> TermsStubPage()
                    else -> StandardOnboardingPage(data = pages[page])
                }
            }
        }

        // ── Controles inferiores: pegadas + botão Próximo ────────────────────
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

// ─── Layout padrão (telas 1–5): imagem grande + título + subtítulo ────────────

@Composable
private fun StandardOnboardingPage(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Imagem ocupa a maior parte do espaço disponível
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

        // Título
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))

        // Subtítulo descritivo (sem balão de fala — SPEC seção 5)
        Text(
            text = data.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Stub: tela 6 — Tema (será implementada na tarefa 3) ─────────────────────

@Composable
private fun ThemeStubPage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp),
        ) {
            Text(
                text = "Escolha o Seu Estilo",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Seletor de tema — será implementado na tarefa 3.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Stub: tela 7 — Termos (será implementada na tarefa 4) ───────────────────

@Composable
private fun TermsStubPage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp),
        ) {
            Text(
                text = "Antes de Começar",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Termos e privacidade — será implementado na tarefa 4.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Botão Próximo com efeito de pressão (mola) ───────────────────────────────

@Composable
private fun NextButton(
    label: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "next_btn_scale",
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth(0.72f)
            .height(52.dp),
        shape = MaterialTheme.shapes.large, // PillShape = 24dp (SPEC seção 3)
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangePrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
