package com.petcare.app.ui.screen.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.petcare.app.ui.theme.OrangePrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Textos legais completos ──────────────────────────────────────────────────

private val PRIVACY_TEXT = """
Política de Privacidade do PetCare
Última atualização: Julho de 2026

O PetCare foi criado para funcionar sem exigir cadastro ou conta de usuário. Não coletamos nem armazenamos seus dados pessoais em nenhum servidor.

Dados armazenados no seu dispositivo: nomes, fotos, datas e informações de saúde dos seus pets, as entradas do Diário e os lembretes ficam salvos apenas no seu aparelho.

Permissões usadas pelo app: Câmera, Galeria e Notificações. Cada permissão só é solicitada no momento do uso.

Publicidade: o PetCare exibe anúncios fornecidos pelo Google AdMob. O AdMob pode coletar identificadores de publicidade do dispositivo.

Conformidade com a LGPD: seguimos os princípios da Lei Geral de Proteção de Dados. Você tem controle total sobre seus dados.
""".trimIndent()

private val TERMS_TEXT = """
Termos de Uso do PetCare
Última atualização: Julho de 2026

1. Natureza do app: ferramenta de organização pessoal para cuidados com pets.

2. Seus dados: armazenados localmente. Você é responsável pelos backups.

3. Anúncios: o app oferece recursos gratuitos com limite de pets, expansível via anúncios.

4. Propriedade intelectual: nome, mascote e design são de nossa propriedade.
""".trimIndent()

// ─── Itens do resumo visual ───────────────────────────────────────────────────

private data class BulletItem(val icon: ImageVector, val text: String)

private val BULLET_ITEMS = listOf(
    BulletItem(Icons.Outlined.Lock,
        "Seus dados ficam só no aparelho — nenhuma conta obrigatória."),
    BulletItem(Icons.Outlined.NotificationsNone,
        "Notificações chegam só quando você criar lembretes."),
    BulletItem(Icons.Outlined.Campaign,
        "O app exibe anúncios para manter os recursos gratuitos."),
)

// ─── Composable principal ─────────────────────────────────────────────────────

@Composable
fun TermsPage(
    isActive: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val scrollState = rememberScrollState()
    val hasReachedEnd by remember { derivedStateOf { !scrollState.canScrollForward } }

    val alphas   = remember { List(BULLET_ITEMS.size) { Animatable(0f) } }
    val offsetsY = remember { List(BULLET_ITEMS.size) { Animatable(26f) } }

    LaunchedEffect(isActive) {
        if (isActive) {
            delay(200)
            BULLET_ITEMS.indices.forEach { i ->
                launch { alphas[i].animateTo(1f, animationSpec = tween(300)) }
                launch { offsetsY[i].animateTo(0f, animationSpec = tween(300)) }
                delay(120)
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Área com scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            Icon(
                imageVector        = Icons.Outlined.Shield,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = Color.White,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text       = "Antes de começar",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = "O PetCare funciona sem cadastro. Veja como cuidamos dos seus dados e como o app funciona.",
                fontSize  = 14.sp,
                color     = Color.White.copy(alpha = 0.88f),
            )

            Spacer(Modifier.height(24.dp))

            BULLET_ITEMS.forEachIndexed { i, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .graphicsLayer {
                            alpha        = alphas[i].value
                            translationY = offsetsY[i].value
                        },
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = null,
                        modifier           = Modifier.size(26.dp),
                        tint               = Color.White,
                    )
                    Text(
                        text     = item.text,
                        fontSize = 14.sp,
                        color    = Color.White.copy(alpha = 0.92f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.20f))
            Spacer(Modifier.height(4.dp))

            TextButton(onClick = { showDialog = true }) {
                Text(
                    text  = "Ler o texto completo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(100.dp))
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

        // Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked         = checked,
                onCheckedChange = { if (hasReachedEnd) onCheckedChange(it) },
                enabled         = hasReachedEnd,
                colors          = CheckboxDefaults.colors(
                    checkedColor            = Color.White,
                    checkmarkColor          = OrangePrimary,
                    uncheckedColor          = Color.White.copy(alpha = 0.70f),
                    disabledUncheckedColor  = Color.White.copy(alpha = 0.30f),
                ),
            )
            Text(
                text     = "Li e aceito os Termos de Uso e a Política de Privacidade.",
                fontSize = 13.sp,
                color    = if (hasReachedEnd) Color.White else Color.White.copy(alpha = 0.45f),
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }

    if (showDialog) {
        FullTextDialog(onDismiss = { showDialog = false })
    }
}

// ─── Diálogo: texto completo ──────────────────────────────────────────────────

@Composable
private fun FullTextDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val contentScrollState = rememberScrollState()

    LaunchedEffect(selectedTab) { contentScrollState.scrollTo(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier      = Modifier.fillMaxWidth().heightIn(max = 560.dp),
            shape         = MaterialTheme.shapes.medium,
            color         = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column {
                Text(
                    text     = "Documentos completos",
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
                    color    = MaterialTheme.colorScheme.onSurface,
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = MaterialTheme.colorScheme.surface,
                    contentColor     = OrangePrimary,
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick  = { selectedTab = 0 },
                        text     = { Text("Privacidade", style = MaterialTheme.typography.labelMedium) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick  = { selectedTab = 1 },
                        text     = { Text("Termos de Uso", style = MaterialTheme.typography.labelMedium) },
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(contentScrollState)
                        .padding(20.dp),
                ) {
                    Text(
                        text  = if (selectedTab == 0) PRIVACY_TEXT else TERMS_TEXT,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(end = 12.dp, bottom = 8.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Fechar", color = OrangePrimary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
