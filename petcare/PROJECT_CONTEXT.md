# PROJECT_CONTEXT.md — PetCare

> Fonte de verdade do progresso. Consultar antes de iniciar ou retomar qualquer tarefa.
> Atualizar a cada etapa concluída com frase curta do que foi feito.

---

## Status Global

| Símbolo | Significado |
|---------|-------------|
| ⬜ | Não iniciado |
| 🔄 | Em andamento |
| ✅ | Concluído e testado |

---

## 0. Configuração inicial

| # | Item | Status | Notas |
|---|------|--------|-------|
| 0.1 | Token do GitHub salvo nos Secrets | ✅ | Secret `GITHUB_TOKEN` no Replit |
| 0.2 | Repositório privado criado no GitHub | ✅ | `silvamirandaheitor007-cloud/petcare` (privado) |
| 0.3 | GitHub Actions — workflow de build do APK | ✅ | `.github/workflows/build.yml` — build passou (debug 22 MB + release 2,9 MB) |
| 0.4 | SPEC.md salvo na raiz do repositório | ✅ | Copiado de `attached_assets/SPEC_*.md` |
| 0.5 | PROJECT_CONTEXT.md criado na raiz | ✅ | Este arquivo |

---

## 1. Configuração técnica do projeto (Android)

| # | Item | Status | Notas |
|---|------|--------|-------|
| 1.1 | Projeto Android criado (Kotlin + Jetpack Compose) | ✅ | Compose BOM 2024.09.02 |
| 1.2 | Room configurado (SQLite) | ✅ | Room 2.6.1, 4 entidades: Pet, Reminder, DiaryEntry, HealthRecord |
| 1.3 | Hilt configurado (injeção de dependência) | ✅ | Hilt 2.51.1 + HiltWorkerFactory |
| 1.4 | Gradle configurado | ✅ | Gradle 8.7, AGP 8.4.2, KSP 2.0.20-1.0.25 |
| 1.5 | Coil configurado (carregamento de imagens) | ✅ | Coil 2.7.0 |
| 1.6 | Fonte Nunito configurada globalmente | ✅ | Google Fonts via Downloadable Fonts API |
| 1.7 | Material Symbols Rounded configurado | ✅ | Material3 + Compose BOM |
| 1.8 | Tema claro/escuro implementado | ✅ | PetCareTheme.kt com ThemeViewModel |
| 1.9 | Grade de espaçamento 8dp configurada | ✅ | Convenção definida, aplicar nas telas |
| 1.10 | Gerenciamento de permissões | ✅ | Accompanist Permissions 0.36.0, receptores no manifesto |

---

## 2. Ícone do aplicativo

| # | Item | Status | Notas |
|---|------|--------|-------|
| 2.1 | Adaptive icon implementado (foreground + background em camadas) | ✅ | `mipmap-anydpi-v26/ic_launcher.xml` referencia background (gradient XML) + foreground (PNG 432×432) |
| 2.2 | Background preenche 108x108dp sem margens | ✅ | `ic_launcher_background.xml`: gradiente #FF9152→#FF5E3A, shape retangular sem padding |
| 2.3 | Foreground dentro da área de segurança 66% (~72x72dp) | ✅ | `icone_app.png` escalado a 288px e centralizado em canvas 432px transparente |
| 2.4 | Ícones legados gerados (hdpi/xhdpi/xxhdpi/xxxhdpi) | ✅ | ic_launcher.png + ic_launcher_round.png em 72/96/144/192px via ImageMagick |
| 2.5 | Testado visualmente em 2 formatos de launcher diferentes | ⬜ | Pendente — usuário irá instalar o APK no celular |

---

## 2.1 Verificação de imagens (35 imagens)

| # | Arquivo | Usada e confirmada |
|---|---------|-------------------|
| 1 | `icone_app.png` | ⬜ |
| 2 | `mascote_splash.png` | ⬜ |
| 3 | `onboarding_1_boasvindas.png` | ✅ |
| 4 | `onboarding_2_meuspets.png` | ✅ |
| 5 | `onboarding_4_fotos.png` | ✅ |
| 6 | `onboarding_3_lembretes.png` | ✅ |
| 7 | `mel_avatar.png` | ✅ |
| 8 | `vazio_meuspets.png` | ⬜ |
| 9 | `vazio_lembretes.png` | ⬜ |
| 10 | `vazio_diario.png` | ⬜ |
| 11 | `vazio_vacinas.png` | ⬜ |
| 12 | `vazio_consultas.png` | ⬜ |
| 13 | `vazio_peso.png` | ⬜ |
| 14 | `vazio_alimentacao.png` | ⬜ |
| 15 | `vazio_medicamentos.png` | ⬜ |
| 16 | `feedback_sucesso.png` | ⬜ |
| 17 | `feedback_desbloquear.png` | ⬜ |
| 18 | `feedback_erro.png` | ⬜ |
| 19 | `feedback_permissao.png` | ⬜ |
| 20 | `mel_avatar_pequeno.png` | ✅ |
| 21 | `avatar_pet_padrao.png` | ⬜ |
| 22 | `icone_especie_cachorro.png` | ⬜ |
| 23 | `icone_especie_gato.png` | ⬜ |
| 24 | `icone_especie_passaro.png` | ⬜ |
| 25 | `icone_especie_peixe.png` | ⬜ |
| 26 | `icone_especie_reptil.png` | ⬜ |
| 27 | `icone_especie_roedor.png` | ⬜ |
| 28 | `icone_especie_outro.png` | ⬜ |
| 29 | `icone_vacina.png` | ⬜ |
| 30 | `icone_consulta.png` | ⬜ |
| 31 | `icone_banho.png` | ⬜ |
| 32 | `icone_medicacao.png` | ⬜ |
| 33 | `icone_alimentacao.png` | ⬜ |
| 34 | `icone_vermifugo.png` | ⬜ |
| 35 | `icone_personalizado.png` | ⬜ |

> **Nota (resolvida em 2026-07-10):** o ZIP contém 36 arquivos — havia 1 extra não listado na tabela do SPEC: `onboarding_temas.png`. Confirmado com o usuário: **não usar** essa imagem em nenhuma tela. A Onboarding 6 (Escolha de tema) usa apenas o seletor nativo Sol/Lua, como já definido no SPEC.md.

---

## 3. Identidade visual

| # | Item | Status | Notas |
|---|------|--------|-------|
| 3.1 | Cores do tema claro definidas | 🔄 | Já existiam em `PetCareTheme.kt`/`colors.xml`: fundo #FFF8F3, laranja #FF7A3D, gradiente #FF9152→#FF5E3A. Aguardando validação visual no celular via `DesignSystemPreviewScreen`. |
| 3.2 | Cores do tema escuro definidas | 🔄 | Já existiam: fundo #1E1A17, cards #2B2420, laranja #FF8C42. Aguardando validação visual. |
| 3.3 | Fonte Nunito em 100% dos textos | 🔄 | `PetCareTypography.kt` (Google Fonts) já cobria todos os estilos do Material3; nenhuma tela real construída ainda usa texto fora do Typography, então cobertura de 100% será reconfirmada tela a tela conforme forem implementadas. |
| 3.4 | Raio de borda 16dp (cards) e 24dp (botões/pills) global | 🔄 | Novo: `PetCareShapes.kt` — `CardShape`/`MaterialTheme.shapes.medium` = 16dp, `PillShape`/`MaterialTheme.shapes.large` = 24dp, aplicado via `PetCareTheme(shapes = PetCareShapes)`. |
| 3.5 | Grade de espaçamento 8dp (token reutilizável) | 🔄 | Novo: `PetCareSpacing.kt` — `MaterialTheme.spacing.{xs,sm,md,lg,xl,xxl}` = 8/16/24/32/40/48dp via `CompositionLocalProvider`. |
| 3.6 | Ícones Material Symbols Rounded configurados | 🔄 | Já existia a dependência `material-icons-extended`; preview usa `Icons.Rounded.*` para confirmar disponibilidade/estilo. |
| 3.7 | Preview de validação da base de design | 🔄 | Novo: `DesignSystemPreviewScreen.kt`, temporariamente acessível pela rota "onboarding" (ver nota abaixo) para inspeção visual no celular antes de qualquer tela real ser construída. Remover essa ligação temporária na tarefa da seção 5. |

---

## 4. Splash screen

| # | Item | Status | Notas |
|---|------|--------|-------|
| 4.1 | Tela Compose customizada (sem tela branca inicial) | ✅ | `Theme.PetCare.Splash` (windowBackground laranja) elimina a tela branca antes do primeiro frame Compose. Confirmado visualmente no celular. |
| 4.2 | Animação: mascote com quique de entrada (escala + overshoot) | ✅ | Escala via `spring(dampingRatio = DampingRatioLowBouncy, stiffness = StiffnessHigh)`. Mascote visível e com quique perceptível, ~910ms (acima da meta de 400-500ms do SPEC, mas aceito pelo usuário — diferença visual pequena). |
| 4.3 | Animação: nome "PetCare" com fade/slide | ✅ | Roda em sequência após o mascote (420ms). |
| 4.4 | Animação: frase "Cuidando dos seus pets com carinho" | ✅ | Roda em sequência após o nome (380ms). |
| 4.5 | Navegação aguarda animação + carregamento do DataStore | ✅ | `animationDone` (fim real das `animateTo`, sem `delay` arbitrário) combinado com `isReady` do `AppViewModel`; navega só quando ambos são verdadeiros. Tempo total até navegar ~1,93s. |

---

## 5. Onboarding (7 telas)

| # | Item | Status | Notas |
|---|------|--------|-------|
| 5.1 | Tela 1 — Boas-vindas (`onboarding_1_boasvindas.png`) | ✅ | Imagem real + título + subtítulo. Testado no celular. |
| 5.2 | Tela 2 — Meus Pets (`onboarding_2_meuspets.png`) | ✅ | Imagem real + título + subtítulo. Testado no celular. |
| 5.3 | Tela 3 — Diário (`onboarding_4_fotos.png`) | ✅ | Imagem real + título + subtítulo. Testado no celular. |
| 5.4 | Tela 4 — Lembretes (`onboarding_3_lembretes.png`) | ✅ | Imagem real + título + subtítulo. Testado no celular. |
| 5.5 | ~~Tela 5 — Assistente Mel (`mel_avatar.png`)~~ | 🗑️ REMOVIDO | Página "Conheça a Mel" removida em 2026-07-18. Onboarding agora tem 6 páginas. |
| 5.6 | Tela 5 (era 6) — Escolha de tema (seletor visual Sol/Lua, sem imagem) | ✅ | Dois cards (LightMode/DarkMode) com borda laranja animada + escala no selecionado. Tema muda ao vivo via DataStore → ThemeViewModel. Preferência persiste. Testado no celular. |
| 5.7 | Tela 6 (era 7) — Termos e privacidade | ✅ | Ícone escudo + "Antes de começar" + 4 tópicos visuais + animação escalonada (graphicsLayer+Animatable) + link "Ler texto completo" + scroll obrigatório libera checkbox + botão desabilitado até aceite. Testado no celular. |
| 5.8 | Transição: slide horizontal + fade entre páginas | ✅ | `graphicsLayer { alpha = (1 - offset * 0.55).coerceIn(0,1) }` dentro do `HorizontalPager`. Testado no celular. |
| 5.9 | Indicador de progresso: pegadas (não bolinhas) | ✅ | `FootprintIndicator` com ícone pata laranja (selecionado) vs cinza (demais). Testado no celular. |
| 5.10 | Botão "Próximo" com efeito de pressão | ✅ | `spring(DampingRatioMediumBouncy)` na escala ao pressionar via `MutableInteractionSource`. Testado no celular. |
| 5.11 | Botão "Pular" (todas exceto Termos) → vai para tela de Termos | ✅ | `animateScrollToPage(termsIndex)`. Oculto na tela 7. Testado no celular. |
| 5.12 | Botão voltar do sistema navega entre páginas | ✅ | `BackHandler(enabled = currentPage > 0)` com `animateScrollToPage(currentPage - 1)`. Testado no celular. |
| 5.13 | Texto completo da Política de Privacidade (seção 19) | ✅ | `PRIVACY_TEXT` em `TermsPage.kt` — aba "Privacidade" no diálogo. Texto exato da seção 19, datado "Julho de 2026". Testado no celular. |
| 5.14 | Texto completo dos Termos de Uso (seção 19) | ✅ | `TERMS_TEXT` em `TermsPage.kt` — aba "Termos de Uso" no diálogo. Texto exato da seção 19, datado "Julho de 2026". Testado no celular. |

---

## 6. Navegação global (5 abas)

| # | Item | Status | Notas |
|---|------|--------|-------|
| 6.1 | Bottom navigation com 5 abas | ✅ | `NavigationBar` Material3 com 5 abas. `MainScreen.kt`. Testado no celular. |
| 6.2 | Cabeçalhos sem imagem do mascote (só texto + gradiente laranja) | ✅ | `PetCareTopBar`: gradiente `OrangeGradStart→OrangeGradEnd`, título branco, sem ilustração. Testado no celular. |
| 6.3 | ~~Botão flutuante Mel~~ | 🗑️ REMOVIDO | `MelFab` removido em 2026-07-18 junto com toda a seção 15. |
| 6.4 | FAB "+" de ação (Meus Pets / Diário / Lembretes) | ✅ | `AddFab` (`SmallFloatingActionButton` 48dp laranja) alinhado ao canto inferior direito. Sem rotação "×" (era vinculado ao sheet do Mel). Exibido apenas nas abas `hasAddFab = true`. |
| 6.5 | Ícone selecionado faz "pulo" (animação) | ✅ | `animateFloatAsState` + `spring(DampingRatioMediumBouncy, StiffnessHigh)` na escala do ícone. Testado no celular. |

---

## 7. Aba Início

| # | Item | Status | Notas |
|---|------|--------|-------|
| 7.1 | Header com saudação por horário (Bom dia/tarde/noite + nome) | ✅ | `PetCareTopBar` em `MainScreen.kt` exibe greeting + `warmPhrase` como subtitle; hora lida via `Calendar`. |
| 7.2 | Card de estatísticas (Total pets / Próxima vacina / Próxima consulta) | ✅ | `StatsCard` 3 colunas com dividers; mostra "--" (sem "Em dia") quando reminders null. `HomeViewModel`. |
| 7.3 | Card de dica do Mel (banco ≥30 dicas cães/gatos, ≥10 demais) | ✅ | `MelTips.kt`: 32 cachorro, 33 gato, 11 pássaro, 10 peixe, 10 réptil, 11 roedor. Baseadas em WSAVA/CFMV. |
| 7.4 | Lista de pets como cards horizontais (foto + nome + dado rápido) | ✅ | `LazyRow` com `PetHorizontalCard` (140dp, foto circular 64dp, nome, idade calculada). Coil + fallback. |
| 7.5 | Estado vazio: `vazio_meuspets.png` centralizada + botão pill animado | ✅ | `EmptyPetsSection`: imagem 68% largura, título caloroso, botão pill OrangePrimary com `InfiniteTransition` escala 1→1.055. |
| 7.6 | Banner AdMob posicionado abaixo, com espaçamento adequado | ✅ | `BannerAdView` via `AndroidView(AdView)`, test ID, 28dp spacer acima. `MobileAds.init()` já no Application. |
| 7.7 | Sem seções "Próximos Lembretes" nem prévia do Diário | ✅ | `HomeScreen` contém apenas: stats, dica, lista de pets / estado vazio, banner. Nenhuma outra seção. |

---

## 8. Aba Meus Pets

| # | Item | Status | Notas |
|---|------|--------|-------|
| 8.1 | Sem chips de ordenação | ✅ | `PetsScreen.kt`: apenas a grade, sem controles de ordenação/filtro. |
| 8.2 | Badge "X/10" integrado ao título | ✅ | `PetCareTopBar` (`MainScreen.kt`) ganhou parâmetro `badge`; pill branca translúcida ao lado do título, populada via `PetsViewModel.petCount` só na aba Meus Pets. |
| 8.3 | Grade 2 colunas de cards (foto, nome, espécie/raça, badge sexo/castração) | ✅ | `LazyVerticalGrid(GridCells.Fixed(2))` + `PetGridCard`; badge de sexo (ícone Male/Female) e castração (ícone ContentCut) via `SexCastrationBadge`. |
| 8.4 | Placeholder `avatar_pet_padrao.png` quando sem foto | ✅ | `AsyncImage` com `fallback`/`error`/`placeholder` = `avatar_pet_padrao`, mesmo padrão do `PetHorizontalCard` da Home. |
| 8.5 | Estado vazio: `vazio_meuspets.png` centralizada | ✅ | `EmptyPetsGridState`: `Box(fillMaxSize, contentAlignment = Center)` — corrige o bug de canto documentado; sem botão (a aba já tem FAB "+" próprio). |
| 8.6 | Botão "+" posicionado corretamente (não sobrepõe Mel) | ✅ | Já satisfeito pela pilha de FABs compartilhada em `MainScreen.kt` (`hasAddFab = true` para `PETS`); ação ainda é TODO no-op — formulário "Novo Pet" é escopo de seção futura. |
| 8.7 | Animação: stagger de entrada nos cards | ✅ | `StaggeredPetCard`: `LaunchedEffect` com delay proporcional ao índice (60ms/item, teto 400ms) + `AnimatedVisibility` (fade + slide vertical). |
| 8.8 | Animação: compressão ao tocar num card | ✅ | `PetGridCard`: `MutableInteractionSource` + `collectIsPressedAsState` → `animateFloatAsState` (escala 0.96 ao pressionar). Tap ainda não navega (detalhe do pet é seção futura). |
| 8.9 | Banner AdMob | ✅ | Cópia privada de `BannerAdView` em `PetsScreen.kt` (mesmo padrão da Home), como item de largura total ao final da grade. |

---

## 9. Aba Diário

| # | Item | Status | Notas |
|---|------|--------|-------|
| 9.1 | Estado vazio: `vazio_diario.png` centralizada | ✅ | `EmptyDiarySection` (`DiaryScreen.kt`): `Box(fillMaxSize, contentAlignment = Center)`, mesmo padrão validado nas seções 7 e 8; sem botão (a aba já tem FAB "+" próprio). |
| 9.2 | Sem chips de filtro por categoria | ✅ | `DiaryScreen.kt` não tem nenhum chip de categoria — apenas o filtro por pet (9.3). |
| 9.3 | Filtro por pet (se houver filtro) | ✅ | `PetFilterRow`: chips "Todos" + um por pet, só exibidos quando há mais de um pet cadastrado (com 0 ou 1 pet a lista não é filtrável, então o chip some). |
| 9.4 | Timeline vertical: foto grande, legenda ≤140 chars, data, pet relacionado | ✅ | `DiaryEntryCard` em `LazyColumn`: foto 240dp, legenda truncada defensivamente em 140 chars (já limitada na origem), data `dd/MM/yyyy`, nome do pet resolvido via `DiaryViewModel.pets`. |
| 9.5 | Botão de compartilhar por entrada | ✅ | `shareDiaryEntry`: `Intent.ACTION_SEND` com a foto via `FileProvider` (`androidx.core.content.FileProvider`, novo provider `${applicationId}.fileprovider` registrado no manifesto) + legenda como texto. |
| 9.6 | Botão de editar/excluir por entrada | ✅ (excluir) / ⬜ (editar) | Excluir: `AlertDialog` de confirmação → `DiaryViewModel.deleteEntry`. Editar (entrada já existente): botão presente mas ainda TODO — fora de escopo desta parte; o editor agora existe, mas só é usado no fluxo de criação (9.8-9.11). |
| 9.7 | Botão "+" abre seletor de galeria | ✅ | `AddFab` em `MainScreen.kt` (`hasAddFab = true` para `DIARY`); clique abre `PickVisualMedia` → ao escolher imagem, navega para `DiaryAddEntryScreen`. |
| 9.8 | ~~Editor de fotos: crop, girar, filtros~~ | 🗑️ REMOVIDO | `DiaryPhotoEditorScreen.kt` e `DiaryPhotoEditorModels.kt` deletados em 2026-07-18. Substituídos pelo fluxo simples `DiaryAddEntryScreen`. |
| 9.9 | ~~Sliders brilho/contraste/saturação~~ | 🗑️ REMOVIDO | Removido junto com o editor de fotos. |
| 9.10 | ~~Adesivos temáticos~~ | 🗑️ REMOVIDO | Removido junto com o editor de fotos. |
| 9.11 | ~~Texto sobre a imagem~~ | 🗑️ REMOVIDO | Removido junto com o editor de fotos. |
| 9.11b | Novo fluxo simplificado do Diário | ✅ | `DiaryAddEntryScreen.kt`: galeria → prévia da foto (AsyncImage, aspecto 1:1) + campo de legenda (até 140 chars) + chips de seleção de pet + botão Salvar. Bitmap carregado em `Dispatchers.IO`, SPEC 17.3 preservada via `saveDiaryPhotoJpeg` (que chama `flattenOnWhiteBackground` internamente). Rota `Screen.DiaryAddEntry("diary_add_entry/{imageUri}")`. |
| 9.12 | Animação: efeito polaroid ao adicionar entrada | ✅ | `PolaroidReveal` (`DiaryScreen.kt`): rotação -9°→0° + escala 0.82→1 com `spring(DampingRatioMediumBouncy)`; só toca para entradas inseridas após o carregamento inicial da tela (`hasLoadedOnce`/`knownEntryIds`). |
| 9.13 | Banner AdMob | ⬜ | Fora de escopo desta tarefa (tratado junto com uma tarefa futura). |

---

## 10. Aba Lembretes

| # | Item | Status | Notas |
|---|------|--------|-------|
| 10.1 | Lista por data: Hoje / Amanhã / Esta semana / Histórico recolhível | ✅ | `RemindersScreen.kt` + `ReminderViewModel.kt`: agrupamento com `groupByDate()` usando Calendar timezone-aware; Histórico recolhível via `AnimatedVisibility` + `toggleHistorico()`. |
| 10.2 | Filtro por pet | ✅ | `PetFilterChips` em `RemindersScreen.kt`: chips "Todos" + um por pet; só exibido quando há mais de um pet; `ReminderViewModel.selectPet()`. |
| 10.3 | Categorias fixas + personalizado | ✅ | 7 categorias (`vacina/consulta/banho/medicacao/alimentacao/vermifugo/personalizado`); grade 4 colunas em `NewReminderScreen.kt`; `String.toCategoryDrawable()` mapeia para os 7 `icone_*.png`. |
| 10.4 | Recorrência: não repete / diária / semanal / mensal | ✅ | `RecurrenceSelector` em `NewReminderScreen.kt` (4 pills horizontais); `String.toRecurrenceLabel()` exibe no card com ícone `Repeat`. |
| 10.5 | Seletor de data e hora com fuso horário correto | ✅ | `DatePickerDialog` (Material3) + `TimePicker` em `Dialog`; preserva hora ao trocar data e vice-versa via `Calendar`. |
| 10.6 | Estado vazio: `vazio_lembretes.png` | ✅ | `ReminderEmptyState` em `RemindersScreen.kt`: `vazio_lembretes.png` centralizada + texto motivacional. |
| 10.7 | Botão "+" posicionado corretamente (não sobrepõe Mel) | ✅ | Reaproveita a pilha FAB de `MainScreen.kt` (`hasAddFab = true` para `REMINDERS`); clique chama `onNavigateToNewReminder(-1L)`. |
| 10.8 | Notificações locais reais (título/corpo contextual) | ✅ | `ReminderBroadcastReceiver.kt`: `contextualBody()` por categoria + nome do pet. Bug raiz corrigido: `NotificationChannels.createChannels()` nunca era chamado — canal não registrado → notificações descartadas silenciosamente no Android 8+. Fix: chamada em `PetCareApplication.onCreate()`. `SCHEDULE_EXACT_ALARM` sem `maxSdkVersion`. Testado no device (Android 16 / HyperOS 3.1). |
| 10.9 | Notificação: foto do pet como imagem grande | ✅ | `loadLargeIcon()`: decodifica `pet_photos/<path>` como Bitmap → `setLargeIcon()`; fallback = PNG da categoria correspondente. |
| 10.10 | Notificação: ícone de categoria | ✅ | Fallback do `loadLargeIcon()` usa `categoryIconRes()` mapeando as 7 categorias para `icone_*.png`. `setSmallIcon` usa `ic_notification` (monochrome vector, obrigatório API 23+). |
| 10.11 | Notificação: botões "Concluir" e "Adiar 1h" | ✅ | `CompleteReminderReceiver`: marca `isCompleted=true` no DB + cancela notif. `SnoozeReminderReceiver`: atualiza `dateTimeMillis += 1h` no DB + reagenda alarme. Ambos usam `goAsync()`. |
| 10.12 | Notificação: vibração com padrão próprio | ✅ | `NotificationChannels.VIBRATION_PATTERN = [0, 300, 120, 300, 120, 600]` — três pulsos com encerramento longo; aplicado no canal E na notificação. |
| 10.13 | Notificação: agrupamento nativo com várias simultâneas | ✅ | `setGroup(GROUP_KEY)` em cada notif + summary notification `setGroupSummary(true)` com `InboxStyle`. |
| 10.14 | BroadcastReceiver para BOOT_COMPLETED (reagendar após reiniciar) | ✅ | `BootReceiver`: corrigido `android:permission="RECEIVE_BOOT_COMPLETED"` no `<receiver>` que bloqueava entrega pelo sistema. Lógica de grace period: lembretes perdidos até 2h durante boot disparam em 5s; mais antigos descartados. Testado no device. |
| 10.15 | Editar e excluir lembrete | ✅ | Editar: `IconButton` → `onNavigateToNewReminder(reminder.id)` → `NewReminderScreen` com `loadReminder(id)`. Excluir: `AlertDialog` de confirmação → `ReminderViewModel.deleteReminder()`. |
| 10.16 | Tela "Novo Lembrete" redesenhada (visual profissional) | ✅ | `NewReminderScreen.kt` (`ui/screen/main/reminders/`): cabeçalho gradiente laranja, grade de categorias, `PetDropdown`, seletores de data/hora, `RecurrenceSelector`, campo de observações, botão salvar com estado de loading. `NewReminderViewModel.kt`. Rota `new_reminder/{reminderId}` no `PetCareNavGraph.kt`. |
| 10.17 | Animação: check com traço desenhado | ✅ | `ReminderCard`: `Animatable(0f→1f)` + `LaunchedEffect(isCompleted)` → Canvas drawing da path do checkmark com `PathEffect.dashPathEffect` (efeito de traço sendo desenhado). Ícone de categoria faz fade-out simultâneo via `Modifier.alpha(1f - progress)`. |
| 10.18 | Animação: swipe com rastro de pegada | ✅ | `ReminderSwipeContainer` + `SwipeToDismissBox` (Material3): swipe direita = concluir (snaps back, estado muda), swipe esquerda = excluir (dismiss animado). `PawTrailBackground` mostra ícone de ação + 3 patas `Icons.Rounded.Pets` que aparecem em cascata conforme `state.progress` aumenta. |
| 10.19 | Banner AdMob | ✅ | `BannerAdView` (mesmo padrão de HomeScreen/PetsScreen, test ID) adicionado como último item do `LazyColumn` em `RemindersScreen.kt`. |

---

## 11. Formulário "Novo Pet" / Editar Pet

| # | Item | Status | Notas |
|---|------|--------|-------|
| 11.1 | Layout redesenhado do zero (grade 8dp, tipografia do app) | ✅ | 3 blocos navegáveis: Informações Básicas, Informações Médicas, Contatos de Emergência (`NewPetScreen.kt`). |
| 11.2 | Foto: seletor da galeria + corte/giro em tela cheia | ✅ | Parte 2: avatar clicável abre `PickVisualMedia` e depois `PetPhotoEditorScreen` (rota própria do NavGraph, reaproveita o `CropRotateStep` do editor do Diário). Foto salva em `pet_photos/` (JPEG, fundo branco — regra 17.3) e exibida de verdade no formulário e no card da lista (`PetsScreen`/`HomeScreen` via Coil). Placeholder `avatar_pet_padrao.png` some assim que uma foto é escolhida. |
| 11.3 | Seletor de espécie: 7 ícones `icone_especie_*` (nunca emoji) | ✅ | |
| 11.4 | Campos — Informações Básicas: Nome*, Foto, Espécie, Sexo, Raça, Nascimento, Peso | ✅ | |
| 11.5 | Campos — Informações Médicas: Tipo Sanguíneo, Alergias, Condições Crônicas, Castrado | ✅ | |
| 11.6 | Campos — Contatos de Emergência: Nome e telefone do veterinário | ✅ | |
| 11.7 | Campos: Microchip (opcional), Observações | ✅ | |
| 11.8 | Validações: nome obrigatório, peso numérico positivo, data não futura | ✅ | |
| 11.9 | Animações sutis nas transições entre campos/seções | ✅ | `AnimatedContent` com slide+fade entre os 3 blocos. |

---

## 12. Sub-abas de saúde do pet

| # | Item | Status | Notas |
|---|------|--------|-------|
| 12.1 | Sub-aba Vacinas: nome, data, lote (opt), lembrete próxima dose | ✅ | Testado no celular — data correta com DateUtils |
| 12.2 | Sub-aba Medicamentos: nome, dosagem, frequência, duração | ✅ | Testado no celular — data correta com DateUtils |
| 12.3 | Sub-aba Consultas: data, motivo, diagnóstico, orientações | ✅ | Testado no celular — data correta com DateUtils |
| 12.4 | Sub-aba Peso: histórico com data + gráfico de linha | ✅ | Estado vazio: `vazio_peso.png`; gráfico Canvas nativo (bezier cúbico + gradiente + anéis); <2 registros → lista + dica "adicione mais uma pesagem"; datas com DateUtils |
| 12.5 | Sub-aba Alimentação: tipo, quantidade por porção, horários | ✅ | Estado vazio: `vazio_alimentacao.png`; card com `icone_alimentacao.png`, chips de horário (split por vírgula), formatação de gramagem; `AddFeedingForm` com 5 campos; DateUtils. Bug vírgula sobrando + quebra de linha nas abas corrigidos e testados |
| 12.6 | Visual redesenhado (não genérico) | ✅ | Cards com ícone da categoria, campo de dados específico por tipo, animação de entrada escalonada — implementado e testado em todas as 5 sub-abas |
| 12.7 | Animações de entrada nos itens de cada lista | ✅ | `StaggeredHealthItem`: delay proporcional ao índice (55ms/item, teto 380ms) + fadeIn + slideInVertically — implementado e testado |

---

## 13. Exclusão de pet — modal customizado

| # | Item | Status | Notas |
|---|------|--------|-------|
| 13.1 | Modal customizado (não diálogo padrão Android) | ✅ | `Dialog` Compose com `Card(shape=RoundedCornerShape(24.dp))` + `feedback_erro.png` 108dp centralizada; ícone de lixeira na TopBar da `PetDetailScreen` abre o modal via `showDeletePetModal`. |
| 13.2 | Mensagem com nome do pet | ✅ | `buildAnnotatedString`: "Tem certeza que quer remover **{nome}** e todo o histórico dele? Essa ação não pode ser desfeita." — nome em `SpanStyle(fontWeight = FontWeight.Bold)`. |
| 13.3 | Botão "Cancelar" (neutro) e "Remover" (vermelho arredondado do app) | ✅ | `OutlinedButton(shape=RoundedCornerShape(24.dp))` neutro + `Button(containerColor=MaterialTheme.colorScheme.error, shape=RoundedCornerShape(24.dp))` vermelho, tipografia `labelLarge` (Nunito) Bold. |

---

## 14. Aba Perfil

| # | Item | Status | Notas |
|---|------|--------|-------|
| 14.1 | Alternância de tema: ícone Lua (tema claro) / Sol (tema escuro) | ✅ | Switch com `Icons.Rounded.DarkMode` (lua) quando claro e `Icons.Rounded.LightMode` (sol) quando escuro no thumb; `animateColorAsState(tween 300ms)` para transição suave de cor; usa `ThemeViewModel.setDarkTheme()`. |
| 14.2 | Exportar backup via SAF (Storage Access Framework) | ✅ | `OpenDocumentTree` → `takePersistableUriPermission` → WAL checkpoint → `DocumentsContract.createDocument` → copia bytes do `getDatabasePath("petcare.db")` para o SAF URI. |
| 14.3 | Mensagem de backup: "Prontinho! Seus dados estão salvos com segurança 🐾" | ✅ | `SnackbarHost` flutuante em `ProfileScreen`; `ProfileUiEvent.ExportSuccess` emite o texto exato do SPEC. |
| 14.4 | Importar backup: pergunta mesclar ou substituir | ✅ | `OpenDocument("*/*")` → `Dialog` com "Mesclar com dados atuais" / "Substituir tudo"; `ProfileViewModel.importBackup(merge)` — lê o .db via SQLiteDatabase nativo, re-insere via DAOs com id=0 + mapeamento oldPetId→newPetId para preservar FKs. |
| 14.5 | Apagar todos os dados: confirmação dupla | ✅ | Dois `Dialog` sequenciais (1ª: aviso; 2ª: "irreversível"); `ProfileViewModel.deleteAllData()` chama `db.clearAllTables()` + `prefs.setUserName("")`. |
| 14.6 | Seções: Política de Privacidade, Termos de Uso, Sobre o PetCare | ✅ | `ExpandableSection` com `AnimatedVisibility(expandVertically+fadeIn, 250ms)`; textos exatos do SPEC §19; sem seção "Suporte"/e-mail. |
| 14.7 | Campo nome do usuário: "Como podemos te chamar?" | ✅ | `OutlinedTextField` com `ImeAction.Done` → `prefs.setUserName()`; persiste em DataStore; aparece na saudação da aba Início. |

---

## 15. Assistente Mel

| # | Item | Status | Notas |
|---|------|--------|-------|
> **🗑️ SEÇÃO 15 COMPLETAMENTE REMOVIDA — 2026-07-18.**
>
> Arquivos deletados: `MelViewModel.kt`, `MelBottomSheet.kt`, `MelTips.kt`, `MelLoadingAnimation.kt`.
> Referências limpas em: `MainScreen.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `OnboardingScreen.kt`, `TermsPage.kt`, `ProfileScreen.kt`, `strings.xml`.
> Nenhum código morto remanescente.

---

## 16. Animações

| # | Item | Status | Notas |
|---|------|--------|-------|
| 16.1 | Splash: mascote com quique de entrada | ✅ | Concluído com seção 4. |
| 16.2 | Onboarding: slide + fade; pegadas no progresso; animação escalonada Termos | ✅ | Concluído com seção 5. |
| 16.3 | Navegação: ícone selecionado faz "pulo" | ✅ | Já implementado em `MainScreen.kt` (`PetCareBottomBar`): `animateFloatAsState(target=1.22f, spring(DampingRatioMediumBouncy, StiffnessHigh))` + `.scale(scale)` no ícone. Estava marcado ⬜ por engano — confirmado no código. |
| 16.4 | FAB: respiração sutil (Mel) 🗑️; "+" vira "×" 🗑️ | 🗑️ REMOVIDO | `MelFab` e sua animação de respiração removidos com a seção 15. `AddFab` simplificado — sem rotação (era vinculada ao `showMelSheet`). |
| 16.5 | Sucesso: `feedback_sucesso.png` bounce + partículas de pegadas | ✅ | `PetSuccessOverlay` em `NewPetScreen.kt`: scrim 65%, `feedback_sucesso.png` com spring de entrada (MediumBouncy) + oscilação contínua −10dp. `PawParticle`: 5 ícones `Pets` em pentágono (72°), delays 200–560ms, fade-out tween 850ms. Auto-dismiss 2500ms. |
| 16.6 | Desbloquear: `feedback_desbloquear.png` efeito "caixa abrindo" | ⬜ | Não implementado. Depende do fluxo rewarded AdMob (seção 18) — naturalmente fica para depois. |
| 16.7 | Lembretes: check com traço desenhado; swipe com rastro de pegada | ✅ | Ambos já implementados em `RemindersScreen.kt`. **Check:** `Canvas` + `PathEffect.dashPathEffect` com `progress 0→1` animando o traço verde. **Swipe:** `PawTrailBackground` com 3 ícones `Icons.Rounded.Pets` em cascata proporcional ao `fraction`. Estava marcado ⬜ por engano. |
| 16.8 | Diário: efeito polaroid | ✅ | Concluído com a seção 9 (parte 1) — `PolaroidReveal` em `DiaryScreen.kt`. |
| 16.9 | Troca de tema: transição suave ~300-400ms | ✅ | `PetCareTheme.kt`: extensão `@Composable ColorScheme.animated()` anima os 16 slots de cor com `animateColorAsState(tween(350ms, FastOutSlowInEasing))`; `PetCareTheme` passa `targetScheme.animated()` ao `MaterialTheme`. |
| 16.10 | ~~Carregamento: animação do Mel em loop~~ | 🗑️ REMOVIDO | `MelLoadingAnimation.kt` deletado. `LoadingBox` em `PetPhotoEditorScreen` agora usa `CircularProgressIndicator(color = OrangePrimary)`. `DiaryPhotoEditorScreen` removido. |

---

## 17. Armazenamento e dados

| # | Item | Status | Notas |
|---|------|--------|-------|
| 17.1 | 100% local (Room + armazenamento interno para fotos) | ✅ | Confirmado na auditoria (2026-07-18): todos os dados ficam no Room (`petcare.db`) e fotos em `context.filesDir/diary_photos/` e `context.filesDir/pet_photos/`. Nenhum `FileOutputStream` ou `Bitmap.compress` fora das duas Utils. A permissão `INTERNET` no manifesto existe exclusivamente para a API de Downloadable Fonts (Nunito, seção 1.6) e futuramente AdMob (seção 18) — nenhum dado do app trafega pela rede. |
| 17.2 | Sem login / sem conta / sem nuvem | ✅ | Confirmado na auditoria (2026-07-18): nenhuma dependência de Firebase, Retrofit, OkHttp, Ktor, Supabase, `AccountManager` ou qualquer SDK de auth/cloud no `build.gradle.kts` ou no código. Grep por `FirebaseAuth`, `FirebaseFirestore`, `@GET`, `@POST`, `login`, `auth` retornou zero resultados em arquivos `.kt`. |
| 17.3 | Bug fix: ao salvar foto, desenhar sobre fundo branco antes de comprimir (sem fundo preto) | ✅ | **Confirmado em 100% dos pontos de salvamento** na auditoria (2026-07-18). `flattenOnWhiteBackground()` em `DiaryPhotoEditorUtils.kt` (função compartilhada): cria bitmap ARGB_8888, `drawColor(Color.WHITE)`, depois desenha a foto. Chamada em: (1) `saveDiaryPhotoJpeg` — fotos do Diário; (2) `savePetPhotoJpeg` em `PetPhotoEditorUtils.kt` — foto de perfil do pet (seção 11). Nenhum outro ponto de `compress(JPEG)` encontrado no projeto. |

---

## 18. Monetização (AdMob)

| # | Item | Status | Notas |
|---|------|--------|-------|
| 18.1 | IDs de teste durante o desenvolvimento | ✅ | Banner: `ca-app-pub-3940256099942544/6300978111`. Rewarded: `ca-app-pub-3940256099942544/5224354917`. Ambos IDs de teste oficiais do Google. |
| 18.2 | Banner nas abas: Início, Meus Pets, Diário e Lembretes | ✅ | Confirmado funcionando no celular em sessões anteriores. `BannerAdView` em cada Screen usando o test ID. |
| 18.3 | Rewarded: ao tentar cadastrar 11º pet, tela de limite + botão assistir anúncio | ✅ | `PetLimitSheet` (ModalBottomSheet) em `PetLimitScreen.kt`: mostra `feedback_desbloquear.png` com animação "caixa abrindo" (SPEC 16.6), explica o limite de 10 pets, botão "Assistir anúncio" carrega `RewardedAd` via `LaunchedEffect`. FAB "+" em `MainScreen.kt` verifica `petCount >= petLimit` antes de navegar — se no limite, abre o sheet. |
| 18.4 | Rewarded: ao completar, liberar +5 vagas | ✅ | **Bug corrigido 2026-07-18:** armazenamento era `Boolean` (true/false) → 2º anúncio era noop (true→true). Migrado para `intPreferencesKey("extra_pet_slots_count")` em `UserPreferencesRepository` com `addExtraSlots(bonus: Int)` que incrementa atomicamente. `PetsViewModel.petLimit` agora é `PET_LIMIT_FREE + extraSlotsCount` (10 + 0/5/10/15…). `hasExtraSlots: StateFlow<Boolean>` removido (confirmado: nunca usado fora do ViewModel). `unlockExtraSlots()` chama `addExtraSlots(5)` a cada anúncio. Resultado: 10→15→20→25 conforme esperado. |
| 18.5 | Fluxo rewarded testado de ponta a ponta | ✅ | **Testado e aprovado no celular — 2026-07-18.** Anúncio de teste assistido 3 vezes seguidas; limite subiu corretamente a cada vez: 10 → 15 → 20 → 25. Badge da TopBar atualizado em tempo real via `StateFlow` sem reiniciar o app. |

---

## 19. Textos legais

| # | Item | Status | Notas |
|---|------|--------|-------|
| 19.1 | Política de Privacidade (exatamente o texto da seção 19) | ✅ | `PRIVACY_TEXT` em `TermsPage.kt` — aba "Privacidade" no diálogo da tela 7. Testado no celular ("Julho de 2026" confirmado). |
| 19.2 | Termos de Uso (exatamente o texto da seção 19) | ✅ | `TERMS_TEXT` em `TermsPage.kt` — aba "Termos de Uso" no diálogo da tela 7. Testado no celular ("Julho de 2026" confirmado). |
| 19.3 | Sobre o PetCare (exatamente o texto da seção 19) | ✅ | `ABOUT_TEXT` em `ProfileScreen.kt` — seção expansível "Sobre o PetCare". |

---

_Última atualização: 2026-07-18 — **Seção 17 concluída via auditoria**. **Seção 18 (AdMob) parcialmente concluída**: 18.1–18.4 implementados (banners já funcionavam; rewarded implementado agora). 18.5 (teste ponta a ponta) aguarda validação no celular. **16.6 implementado junto** com 18.3: animação "caixa abrindo" para `feedback_desbloquear.png` em `PetLimitScreen.kt`._

_Última atualização: 2026-07-18 — **Bugfix §18.4 (extraSlots booleano → contador):** `UserPreferencesRepository` migrou de `booleanPreferencesKey("extra_pet_slots")` para `intPreferencesKey("extra_pet_slots_count")`; novo método `addExtraSlots(bonus)` incrementa atomicamente. `PetsViewModel` usa `extraSlotsCount` no cálculo de `petLimit` (10 + extras). `hasExtraSlots: StateFlow<Boolean>` removido. Limite agora sobe 10→15→20→25… a cada anúncio._

_Última atualização: 2026-07-20 — **Fix: placeholder de foto por espécie + preenchimento do círculo:**_
_1. **NewPetScreen:** ao selecionar espécie mas ainda não ter foto, o avatar mostra o ícone da espécie centralizado em fundo laranja — muda dinamicamente conforme a espécie é selecionada. Sem espécie selecionada, mostra `avatar_pet_padrao.png` com `ContentScale.Crop` para preencher 100% do círculo._
_2. **PetsScreen (grade 2 colunas):** quando pet não tem foto, mostra ícone da espécie centralizado em fundo laranja suave — remove o `avatar_pet_padrao` genérico._
_3. **HomeScreen (cards horizontais):** círculo laranja com ícone da espécie quando pet sem foto; foto real com `ContentScale.Crop` para preenchimento total._

_Última atualização: 2026-07-20 — **Melhorias no onboarding (duas juntas):**_
_1. **Tela de tema redesenhada:** `ThemeSelectionPage` substituída por mini cards de preview visual — cada card mostra as cores reais do tema (fundo, header laranja, cards internos). Borda branca destaca o selecionado, label confirma "Tema Claro/Escuro ativado". O tema muda ao vivo ao tocar no card._
_2. **Imagem da tela 2 trocada:** `onboarding_2_meuspets.png` (mascote deitado com guia/coleira — imagem triste) substituída por `onboarding_1_boasvindas.png` (mascote acenando alegremente), que não estava sendo usada no tutorial._
_3. **GitHub Actions configurado:** `.github/workflows/build.yml` criado — build automático do APK (debug + release) a cada push no main._

_Última atualização: 2026-07-18 — **Seção 18 (AdMob) aprovada e testada por completo no celular.** §18.5 validado: anúncio assistido 3 vezes seguidas, limite subiu 10→15→20→25 corretamente a cada vez. Toda a seção 18 ✅._

_Última atualização: 2026-07-18 — **Três mudanças estruturais:**_
_1. **Seção 15 (Mel) removida por completo** — 4 arquivos deletados, referências limpas em 7 arquivos. Zero código morto._
_2. **Editor de fotos do Diário removido** — `DiaryPhotoEditorScreen.kt` + `DiaryPhotoEditorModels.kt` deletados. `DiaryPhotoEditorUtils.kt` reduzido às 5 funções essenciais. Novo fluxo simplificado: `DiaryAddEntryScreen.kt` (galeria → prévia + legenda + pet → salvar). SPEC 17.3 (fundo branco) preservada. `CropRotateStep` movido para `PetPhotoEditorScreen.kt` (ainda usado pelo editor de foto de perfil)._
_3. **Diagnóstico de performance** — investigado e reportado. Fixes pendentes aprovação do usuário (ver relatório abaixo)._

---

## Correção transversal: proteção contra duplo-toque nos botões Salvar

| # | Local | Correção | Status |
|---|-------|----------|--------|
| DT.1 | `NewPetViewModel.savePet()` | `savingInProgress` flag — guard síncrono antes da coroutine; bloqueia 2º toque mesmo se recompose ainda não aconteceu | ✅ 2026-07-18 |
| DT.2 | `PetDetailViewModel.insertRecord()` | Mesmo padrão de guard síncrono — bloqueia chamadas duplicadas ao DAO | ✅ 2026-07-18 |
| DT.3 | `AddVaccineForm` (PetDetailScreen) | `var isSaving` local + `enabled = isValid && !isSaving` + `return@Button` no onClick | ✅ 2026-07-18 |
| DT.4 | `AddMedicationForm` (PetDetailScreen) | Idem | ✅ 2026-07-18 |
| DT.5 | `AddConsultationForm` (PetDetailScreen) | Idem | ✅ 2026-07-18 |
| DT.6 | `AddWeightForm` (PetDetailScreen) | Idem | ✅ 2026-07-18 |
| DT.7 | `AddFeedingForm` (PetDetailScreen) | Idem | ✅ 2026-07-18 |

> `NewReminderScreen`, `DiaryPhotoEditorScreen` e `PetPhotoEditorScreen` já tinham proteção correta — não alterados.
> **Teste obrigatório pelo usuário:** (1) tocar várias vezes rápido em "Salvar" do Novo Pet — não duplicar; (2) repetir nos 5 formulários de saúde — nenhum deve criar registro duplicado.

---

## Histórico de melhorias visuais (sessão de redesign)

| # | Tela / Componente | Status | Detalhes |
|---|-------------------|--------|----------|
| 1 | Splash screen | ✅ 2026-07-20 | Redesign completo |
| 2 | Tutorial / Onboarding | ✅ 2026-07-20 | Redesign completo |
| 3 | Aba Início (Home) | ✅ 2026-07-20 | Redesign completo |
| 4 | Aba Meus Pets (grid de cards) | ✅ 2026-07-20 | Cards em grade, animação escalonada, foto por espécie |
| 5 | PetDetailScreen (abas de saúde) + ajustes PetsScreen | ✅ 2026-07-21 | Ver detalhes abaixo |
| 6 (redesign anterior) | DiaryScreen, DiaryAddEntry, ícone Home, fix Próxima Consulta | ✅ 2026-07-21 | Ver Melhoria 7 abaixo |

### Melhoria #5 — detalhes (2026-07-21)

**Ajustes em `PetsScreen.kt`:**
- Imagem do estado vazio de Meus Pets: `vazio_meuspets` → `mascote_splash`
- Label de idade: `"< 1 mês"` → `"Filhote"` (sem símbolo `<`)
- Card do pet: espécie centralizada abaixo do nome (ícone + texto), removido badge do canto superior esquerdo

**Redesign de `PetDetailScreen.kt`:**
- **TopBar / header:** `Column` com gradiente vertical, foto circular 80dp centralizada, nome em bold, chips de espécie / sexo / castrado / idade
- **Tabs:** `ScrollableTabRow` com ícone + label em cada aba (cores: laranja quando ativa, cinza quando inativa)
- **Cards de registros:** barra de acento colorida à esquerda + badge de ícone com fundo colorido translúcido, usando `IntrinsicSize.Min` para altura dinâmica
  - Vacinas = verde `#4CAF50`; Medicamentos = roxo `#9C27B0`; Consultas = azul `#2196F3`
  - Peso = laranja `OrangePrimary`; Alimentação = teal `#00897B`
- **Formulários de novo registro:** `FormHeader` com gradiente laranja, ícone no badge branco translúcido + título branco bold

**Próxima melhoria sugerida (#6):** Simplificar `NewPetScreen` de 3 etapas para 1 tela única (usuário confirmar antes de implementar).

---

## Melhoria 7 — Diário redesenhado + ícone Home + fix Próxima Consulta (2026-07-21)

### Mudanças implementadas

**`MainScreen.kt`**
- Aba Início: `Icons.Rounded.Home` → `Icons.Rounded.Favorite` (coração laranja — mais quente e identitário)
- Aba Diário: `Icons.Rounded.AutoStories` → `Icons.Rounded.PhotoLibrary` (mais representativo de memórias/fotos)

**`DiaryScreen.kt` — redesign completo dos cards**
- Header com contador de memórias ("📸 X memórias guardadas")
- Cards redesenhados: foto aumentada para 260dp, gradiente escurecido na parte inferior para legibilidade
- Badge do nome do pet sobreposto na foto (canto inferior esquerdo, pill laranja)
- Badge de data amigável na foto (canto superior direito, pill escuro): "Hoje" / "Ontem" / nome do dia da semana / dd/MM/yyyy
- Caption formatada como citação `"texto"` com alpha 0.82
- Separador sutil entre caption e barra de ações
- Botão "Compartilhar" em destaque laranja (icon + label), com edit/delete como IconButtons discretos
- Texto de share temático: linha de título `💛 NOME — Diário PetCare`, citação da caption, data, hashtags `#PetCare #NomePet #MeuPet #AmorPelosPets`

**`DiaryAddEntryScreen.kt` — redesign da tela de nova entrada**
- Header com gradiente laranja horizontal (OrangeGradStart → OrangeGradEnd), título "Nova memória" + subtítulo
- Frame polaroid aprimorado: fundo creme `#F5F0E8`, sombra 12dp, padding interno, label `📸 PetCare` no rodapé
- 8 tags de momento (era 6): Passeio, Brincadeira, Banho, Consulta, Carinho, Especial, Ao ar livre, Petisco
- Tags em `FlowRow` com animação de cor (laranja selecionado, cinza não selecionado), ✓ ao selecionar
- Campo de legenda com `OutlinedTextField` arredondado e barra de progresso colorida (verde→laranja→vermelho conforme limite)
- Seletor de pet em FlowRow animado (igual ao padrão das tags)
- Botão salvar com animação de escala (comprime a 0.96f ao processar)

**`HealthRecordDao.kt`**
- Novo método `getUpcomingConsultations(nowMillis: Long): Flow<List<HealthRecord>>`
- Filtra `type = 'consultation'` com `dateMillis > nowMillis`, ordenado ASC

**`HomeViewModel.kt`**
- `nextConsultDate` agora combina **duas fontes** (igual ao comportamento de `nextVaccineDate`):
  1. Lembretes com `category == "consulta"` (criados via aba Lembretes)
  2. HealthRecords `type = "consultation"` com `dateMillis` futuro (criados em PetDetailScreen)
- Retorna a data mais próxima entre as duas fontes

### APK gerado
- Build #84 — sucesso ✅
- petcare-release-signed: 9.2 MB
- Expiração dos artifacts: 2026-07-28
