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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.R
import com.petcare.app.ui.theme.OrangePrimary
import com.petcare.app.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// ─── Dados das páginas ────────────────────────────────────────────────────────

private fun buildPages(): List<OnboardingPageData> = listOf(
    OnboardingPageData(
        imageRes = R.drawable.mascote_splash,
        title    = "Bem-vindo ao PetCare!",
        subtitle = "Aqui começa uma nova forma de cuidar dos seus pets — com carinho, organização e muita alegria.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.onboarding_2_meuspets,
        title    = "Seus pets em um só lugar",
        subtitle = "Cadastre todos os seus companheiros, adicione fotos, registre a espécie, raça e data de nascimento. Organize tudo com carinho!",
    ),
    OnboardingPageData(
        imageRes = R.drawable.onboarding_4_fotos,
        title    = "Guarde cada momento especial",
        subtitle = "Fotos, memórias e histórias dos seus pets em um diário bonito, só para vocês.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.onboarding_3_lembretes,
        title    = "Nunca esqueça um cuidado",
        subtitle = "Vacinas, consultas e remédios — lembretes que chegam na hora certa, sem complicação.",
    ),
    OnboardingPageData(
        imageRes    = null,
        title       = "Escolha o seu estilo",
        subtitle    = "Você pode mudar quando quiser na aba Perfil.",
        isThemePage = true,
    ),
    OnboardingPageData(
        imageRes    = null,
        title       = "Antes de começar",
        subtitle    = "",
        isTermsPage = true,
    ),
)

// ─── Tela principal ───────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages      = remember { buildPages() }
    val totalPages = pages.size
    val termsIndex = totalPages - 1

    val pagerState  = rememberPagerState(pageCount = { totalPages })
    val scope       = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    val selectedDark by viewModel.selectedDark.collectAsState()
    val termsChecked by viewModel.termsChecked.collectAsState()

    BackHandler(enabled = currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangePrimary)
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Pager ────────────────────────────────────────────────────────
            HorizontalPager(
                state                   = pagerState,
                modifier                = Modifier.weight(1f).fillMaxWidth(),
                beyondViewportPageCount = 1,
            ) { page ->
                val offset = ((pagerState.currentPage - page).toFloat() +
                    pagerState.currentPageOffsetFraction).absoluteValue
                val alpha = (1f - offset * 0.55f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { this.alpha = alpha },
                ) {
                    when {
                        pages[page].isThemePage -> ThemeSelectionPage(
                            data         = pages[page],
                            selectedDark = selectedDark,
                            onSelect     = { viewModel.selectTheme(it) },
                        )
                        pages[page].isTermsPage -> TermsPage(
                            isActive        = pagerState.currentPage == page,
                            checked         = termsChecked,
                            onCheckedChange = { viewModel.setTermsChecked(it) },
                        )
                        else -> StandardOnboardingPage(data = pages[page])
                    }
                }
            }

            // ── Controles inferiores ─────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp, top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Pontinhos simples
                DotsIndicator(pageCount = totalPages, currentPage = currentPage)

                // Botão
                val isLast   = currentPage == termsIndex
                val btnLabel = if (isLast) "Aceitar e continuar" else "PRÓXIMO"
                NextButton(
                    label   = btnLabel,
                    enabled = if (isLast) termsChecked else true,
                    onClick = {
                        if (isLast) {
                            viewModel.completeOnboarding()
                            onFinished()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                        }
                    },
                )
            }
        }
    }
}

// ─── Página padrão — imagem no topo, textos brancos ──────────────────────────

@Composable
private fun StandardOnboardingPage(data: OnboardingPageData) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.5f))

        // Imagem / mascote
        data.imageRes?.let { res ->
            Image(
                painter            = painterResource(id = res),
                contentDescription = data.title,
                modifier           = Modifier.size(220.dp),
                contentScale       = ContentScale.Fit,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Título
        Text(
            text      = data.title,
            fontSize  = 22.sp,
            fontWeight = FontWeight.Bold,
            color     = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        // Subtítulo
        Text(
            text      = data.subtitle,
            fontSize  = 14.sp,
            color     = Color.White.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.weight(1f))
    }
}

// ─── Tela de seleção de tema ──────────────────────────────────────────────────

@Composable
private fun ThemeSelectionPage(
    data: OnboardingPageData,
    selectedDark: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text       = data.title,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            textAlign  = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = data.subtitle,
            fontSize  = 14.sp,
            color     = Color.White.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThemeOptionButton(
                label      = "☀️  Claro",
                isSelected = !selectedDark,
                onClick    = { onSelect(false) },
                modifier   = Modifier.weight(1f),
            )
            ThemeOptionButton(
                label      = "🌙  Escuro",
                isSelected = selectedDark,
                onClick    = { onSelect(true) },
                modifier   = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.05f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "theme_scale",
    )
    Button(
        onClick   = onClick,
        modifier  = modifier.scale(scale).height(52.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.20f),
            contentColor   = if (isSelected) OrangePrimary else Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(if (isSelected) 4.dp else 0.dp),
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

// ─── Indicador de pontinhos ───────────────────────────────────────────────────

@Composable
private fun DotsIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { i ->
            val isActive = i == currentPage
            val size by animateFloatAsState(
                targetValue   = if (isActive) 10f else 7f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label         = "dot_size_$i",
            )
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .background(
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.38f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}

// ─── Botão PRÓXIMO ────────────────────────────────────────────────────────────

@Composable
private fun NextButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "next_btn_scale",
    )

    Button(
        onClick           = onClick,
        enabled           = enabled,
        interactionSource = interactionSource,
        modifier          = Modifier
            .scale(scale)
            .fillMaxWidth(0.75f)
            .height(52.dp),
        shape  = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color.White,
            contentColor           = OrangePrimary,
            disabledContainerColor = Color.White.copy(alpha = 0.35f),
            disabledContentColor   = Color.White.copy(alpha = 0.60f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = 0.dp,
            pressedElevation  = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Text(
            text       = label,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp,
            letterSpacing = 0.5.sp,
        )
    }
}
