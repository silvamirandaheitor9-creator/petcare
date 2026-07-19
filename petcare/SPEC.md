# PetCare — Documento mestre definitivo (projeto do zero)

<!--
NOTA PARA O REPLIT AGENT (Replit AI):
Este arquivo é o SPEC.md oficial e a ÚNICA fonte de verdade deste projeto.
Leia o documento inteiro, do início ao fim, antes de escrever qualquer código.
Nenhuma seção é opcional. Nenhuma seção deve ser resumida, simplificada ou pulada.
Se qualquer instrução parecer ambígua, pare e pergunte ao usuário antes de assumir uma interpretação.
Consulte também o PROJECT_CONTEXT.md (seção 0.3) antes de iniciar ou retomar qualquer tarefa.
-->

Este é o único documento de referência para a construção do PetCare. Leia por completo antes de escrever qualquer código. Cada seção é obrigatória e específica — não simplifique, não presuma, não pule etapas. Se algo não estiver claro, pergunte antes de assumir.

> **Stack obrigatória, sem exceção:** Kotlin nativo + Jetpack Compose + Room + Hilt + Gradle. Não usar React Native, Expo, Flutter, Kotlin Multiplatform ou qualquer camada de abstração multiplataforma — nem como ponto de partida, nem como sugestão de "forma mais rápida".

## Índice

- [0. Como trabalhar neste projeto (Diretrizes para o Replit Agent)](#0-como-trabalhar-neste-projeto-diretrizes-para-o-replit-agent)
  - [0.1 Ciclo de Trabalho do Agente](#01-ciclo-de-trabalho-do-agente)
  - [0.2 Regras de Ouro](#02-regras-de-ouro)
  - [0.3 Configuração inicial (antes de qualquer código)](#03-configuração-inicial-antes-de-qualquer-código)
- [1. Configuração técnica do projeto](#1-configuração-técnica-do-projeto)
- [2. Ícone do aplicativo — correção definitiva](#2-ícone-do-aplicativo--correção-definitiva)
  - [2.1 Tabela completa de imagens (todas as 35, sem exceção)](#21-tabela-completa-de-imagens-todas-as-35-sem-exceção)
- [3. Identidade visual](#3-identidade-visual)
- [4. Splash screen](#4-splash-screen)
- [5. Onboarding (7 telas)](#5-onboarding-7-telas)
- [6. Regra global de navegação (vale para as 5 abas)](#6-regra-global-de-navegação-vale-para-as-5-abas)
- [7. Aba Início](#7-aba-início)
- [8. Aba Meus Pets](#8-aba-meus-pets)
- [9. Aba Diário](#9-aba-diário)
- [10. Aba Lembretes](#10-aba-lembretes)
- [11. Formulário "Novo Pet" / Editar Pet — redesign completo](#11-formulário-novo-pet--editar-pet--redesign-completo)
- [12. Sub-abas de saúde do pet (Vacinas, Consultas, Peso, Alimentação, Medicamentos)](#12-sub-abas-de-saúde-do-pet-vacinas-consultas-peso-alimentação-medicamentos)
- [13. Exclusão de pet — tela customizada](#13-exclusão-de-pet--tela-customizada)
- [14. Aba Perfil](#14-aba-perfil)
- [15. Assistente Mel — recriar do zero](#15-assistente-mel--recriar-do-zero)
- [16. Animações — especificação temática completa](#16-animações--especificação-temática-completa)
- [17. Armazenamento e dados](#17-armazenamento-e-dados)
- [18. Monetização (AdMob)](#18-monetização-admob)
- [19. Textos legais (usar exatamente este conteúdo)](#19-textos-legais-usar-exatamente-este-conteúdo)
- [Observação final](#observação-final)

---

## 0. Como trabalhar neste projeto (Diretrizes para o Replit Agent)

Este projeto **NUNCA** deve ser construído inteiro em uma única tarefa. Você vai receber este documento uma vez, no início, como referência permanente — mas a execução acontece em **tarefas pequenas e sequenciais**, uma por mensagem, definidas pelo usuário. Ler este documento inteiro é obrigatório; implementar tudo de uma vez não é permitido, mesmo que pareça mais rápido.

### 0.1 Ciclo de Trabalho do Agente (repete a cada tarefa)
1. **Escopo:** No início de cada tarefa, releia apenas a(s) seção(ões) deste `SPEC.md` relevantes para ela, mais o `PROJECT_CONTEXT.md` inteiro (seu histórico de progresso).
2. **Execução:** Implemente **somente** o que foi pedido na tarefa atual — uma tela ou funcionalidade por vez. Não adiante nem "aproveite" para mexer em outras partes.
3. **Validação real:** Compilar sem erro **não é o critério de pronto**. Rode o app de verdade (emulador/dispositivo), clique nos botões, percorra o fluxo completo, confira visualmente contra a descrição da seção. Só depois disso a tarefa está concluída.
4. **Checkpoint:** Confirmada a tarefa como 100% funcional e sem crashes, crie um **Checkpoint** no Replit antes de seguir para a próxima.
5. **Atualize o `PROJECT_CONTEXT.md`** marcando o item como concluído e testado, com uma frase curta do que foi feito.

### 0.2 Regras de Ouro
- **Não presuma:** se algo não estiver claro, pare e pergunte ao usuário antes de decidir sozinho.
- **Detalhes técnicos são obrigatórios, não sugestões:** cores hexadecimais, raios de borda, nomes de arquivo exatos — use exatamente o que está escrito, nunca uma aproximação.
- **Uma tarefa por vez, sempre.** Se o pedido do usuário parecer grande demais para uma sessão, proponha dividir em partes antes de começar a codificar.

### 0.3 Configuração inicial (antes de qualquer código)

1. Peça o meu token de acesso do GitHub e salve nos **Secrets** do Replit (nunca direto no código).
2. Não existe nenhum repositório para este projeto ainda — crie um **repositório privado novo** no GitHub usando esse token.
3. Configure o **GitHub Actions** desde o primeiro commit: crie o workflow de build automático do APK (`.github/workflows/build.yml`), que roda a cada push na branch principal.
4. Salve este documento inteiro como `SPEC.md` na raiz do repositório — ele é a fonte de verdade e deve continuar acessível a qualquer momento, não depender da sua memória de conversa.
5. Crie também o arquivo `PROJECT_CONTEXT.md` na raiz, com a lista de todas as telas/funcionalidades exigidas em `SPEC.md`, cada uma marcada como "não iniciado", "em andamento" ou "concluído e testado". Atualize esse arquivo a cada etapa — é sua memória de progresso, e você deve consultá-lo antes de mexer em qualquer parte para não recriar nem contradizer trabalho anterior.
6. **Antes de escrever qualquer código**, leia o `SPEC.md` inteiro do início ao fim, sem pular nada. Depois, escreva um resumo próprio, seção por seção (as 19 seções + tabela de imagens), confirmando que entendeu cada uma — e me mostre esse resumo antes de começar a codificar. **Não comece nenhuma tela ou funcionalidade até eu responder dizendo qual é a primeira tarefa.**

---

## 1. Configuração técnica do projeto

- Kotlin + Jetpack Compose, Room (SQLite), Hilt para injeção de dependência.
- Repositório novo no GitHub, criado do zero (ver seção 0.3).
- GitHub Actions configurado desde o primeiro commit para build automático do APK a cada push, com monitoramento (`gh run watch`) até o resultado (sucesso ou falha) ser confirmado.
- **Performance:** o app não pode travar. Evite recomposições desnecessárias em Compose (use `remember`, `derivedStateOf`, chaves estáveis em listas), carregue imagens com Coil (cache automático), evite operações pesadas na thread principal (toda leitura/escrita de banco e arquivo deve rodar em coroutines com `Dispatchers.IO`). Teste a fluidez de rolagem em listas longas (Meus Pets, Diário, Lembretes) antes de considerar pronto.
- **Gerenciamento de Permissões:**
    *   O app deve solicitar permissão de **Notificações** (Android 13+) para os lembretes de saúde.
    *   O app deve solicitar permissão de **Câmera** e **Galeria** para o Diário e fotos do Pet.
    *   As permissões devem ser solicitadas de forma contextual: peça a permissão apenas quando o usuário tentar usar a função pela primeira vez, explicando o motivo com um diálogo amigável antes do prompt do sistema.
    *   Caso o usuário negue, mostre a tela de feedback `feedback_permissao.png` explicando como habilitar nas configurações.

---

## 2. Ícone do aplicativo — correção definitiva

Esse item já falhou duas vezes (uma vez cortado, uma vez pequeno demais). **A imagem `icone_app.png` já está pronta e é definitiva — você não precisa e não deve criar nenhuma versão nova, alternativa ou "melhorada" dela.** Sua única responsabilidade aqui é técnica: implementar a estrutura do ícone corretamente para que ela se adapte a qualquer formato de launcher (círculo, quadrado arredondado, "squircle", gota) sem cortar e sem sobrar espaço vazio. Estratégia obrigatória:

- Separe a imagem original em duas camadas conceituais: um **background** (o quadrado com gradiente laranja/vermelho) que preenche 100% do canvas de 108x108dp **sem nenhuma margem e sem bordas arredondadas no arquivo fonte** — o background deve ser um quadrado sólido que "vaza" para fora da área visível para garantir que, não importa qual máscara o launcher aplique, nunca sobre um espaço vazio ou branco nas bordas. 
- O **foreground** (o coração + pata) deve ser posicionado dentro da **área de segurança de ~66% central** do canvas (aproximadamente 72x72dp centrais), garantindo que nenhum detalhe do ícone principal seja cortado.
- **Atenção crucial:** certifique-se de que o `ic_launcher_background.xml` (ou o drawable correspondente) não possua nenhum padding interno ou cor de fundo secundária (como branco) que possa aparecer sob o gradiente. O gradiente deve ser a única camada de fundo e deve ser esticado para cobrir todo o contêiner.
- Implementar via `res/mipmap-anydpi-v26/ic_launcher.xml` (e `ic_launcher_round.xml`) referenciando essas duas camadas como `ic_launcher_background` e `ic_launcher_foreground`.
- Gerar os ícones legados (`mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi`) a partir dessa mesma composição de camadas, não como PNGs exportados isoladamente com padding diferente cada um — isso foi o que causou inconsistência de tamanho nas tentativas anteriores.
- Depois de implementado, gerar o APK, instalar de verdade num aparelho/emulador, e conferir visualmente em pelo menos duas formas de launcher diferentes (se possível) que o ícone preenche o espaço de forma equilibrada — não presumir que está correto só pelo código.

---

## 2.1 Tabela completa de imagens (todas as 35, sem exceção)

Esta tabela reflete o conteúdo visual real de cada arquivo após a renomeação corretiva.

| Arquivo | Descrição Visual (O que deve estar na foto) | Uso no app |
|---|---|---|
| `icone_app.png` | Quadrado com gradiente e coração com pata | Ícone do aplicativo (adaptive icon) |
| `mascote_splash.png` | Mascote inteiro em composição vertical | Splash screen |
| `onboarding_1_boasvindas.png` | Mascote acenando alegremente | Onboarding 1 — Boas-vindas |
| `onboarding_2_meuspets.png` | Mascote deitado ao lado de uma guia/coleira | Onboarding 2 — Meus Pets |
| `onboarding_4_fotos.png` | Mascote segurando uma câmera | Onboarding 3 — Diário (Fotos) |
| `onboarding_3_lembretes.png` | Mascote ao lado de um despertador | Onboarding 4 — Lembretes |
| `mel_avatar.png` | Mascote em pose pensativa (mão no queixo) | Onboarding 5 — Assistente Mel |
| (Não utilizar imagem) | N/A | Onboarding 6 — Escolha de tema (Usar seletor visual nativo) |
| `vazio_meuspets.png` | Mascote dentro de um círculo (estilo avatar) | Estado vazio — aba Meus Pets |
| `vazio_lembretes.png` | Calendário ou bloco de notas em branco | Estado vazio — aba Lembretes |
| `vazio_diario.png` | Moldura de foto polaroid vazia | Estado vazio — aba Diário |
| `vazio_vacinas.png` | Ilustração grande de uma seringa | Estado vazio — sub-aba Vacinas |
| `vazio_consultas.png` | Ilustração grande de um estetoscópio | Estado vazio — sub-aba Consultas |
| `vazio_peso.png` | Ilustração grande de uma balança | Estado vazio — sub-aba Peso |
| `vazio_alimentacao.png` | Ilustração grande de uma tigela de comida | Estado vazio — sub-aba Alimentação |
| `vazio_medicamentos.png` | Ilustração grande de um frasco de remédio | Estado vazio — sub-aba Medicamentos |
| `feedback_sucesso.png` | Mascote comemorando (feliz/vibrando) | Feedback — Sucesso (Pet adicionado, etc.) |
| `feedback_desbloquear.png` | Cadeado aberto com presente e símbolos | Feedback — Desbloquear mais pets |
| `feedback_erro.png` | Mascote triste com ponto de interrogação | Feedback — Erro genérico |
| `feedback_permissao.png` | Mascote apontando para uma engrenagem | Feedback — Permissão negada |
| `mel_avatar_pequeno.png` | Rosto do mascote em formato circular pequeno | Ícone do botão flutuante (Assistente) |
| `avatar_pet_padrao.png` | Cachorrinho chibi frontal (placeholder) | Placeholder de foto do pet |
| `icone_especie_cachorro.png` | Cabeça de cachorro simplificada | Seletor de espécie — Cachorro |
| `icone_especie_gato.png` | Cabeça de gato simplificada | Seletor de espécie — Gato |
| `icone_especie_passaro.png` | Cabeça de pássaro simplificada | Seletor de espécie — Pássaro |
| `icone_especie_peixe.png` | Peixe simplificado | Seletor de espécie — Peixe |
| `icone_especie_reptil.png` | Réptil/Lagarto simplificado | Seletor de espécie — Réptil |
| `icone_especie_roedor.png` | Hamster/Roedor simplificado | Seletor de espécie — Roedor |
| `icone_especie_outro.png` | Pata ou símbolo genérico | Seletor de espécie — Outro |
| `icone_vacina.png` | Ícone pequeno de seringa | Categoria — Vacina |
| `icone_consulta.png` | Ícone pequeno de estetoscópio | Categoria — Consulta |
| `icone_banho.png` | Ícone pequeno de gota d'água | Categoria — Banho |
| `icone_medicacao.png` | Ícone pequeno de cápsula/comprimido | Categoria — Medicação |
| `icone_alimentacao.png` | Ícone pequeno de pote de ração | Categoria — Alimentação |
| `icone_vermifugo.png` | Ícone pequeno de cápsula (específico) | Categoria — Vermífugo |
| `icone_personalizado.png` | Ícone pequeno de estrela | Categoria — Personalizado |

**Checklist de verificação obrigatória:** depois de posicionar todas as imagens, confira uma por uma que cada arquivo está realmente sendo referenciado no código (não basta o arquivo existir na pasta `drawable` — precisa estar de fato chamado no componente certo). Marque essa verificação como um item específico no `PROJECT_CONTEXT.md`, com uma lista das 35 imagens e uma marcação de "usada e confirmada" em cada uma.

---

## 3. Identidade visual

- **Nome do app:** PetCare
- **Mascote:** Mel, filhote de cachorro bicolor marrom/branco, estilo chibi com contorno grosso, cel-shading.
- **Cores (tema claro):** fundo `#FFF8F3`, cards brancos, laranja primário `#FF7A3D`, gradiente `#FF9152 → #FF5E3A`.
- **Cores (tema escuro):** fundo `#1E1A17`, cards `#2B2420`, laranja `#FF8C42`.
- **Tipografia:** fonte **Baloo 2** ou **Nunito** (Google Fonts) aplicada em 100% dos textos do app — nenhum texto deve usar a fonte padrão do sistema.
- **Ícones de interface** (não as ilustrações do mascote): Material Symbols Rounded, nunca misturado com outro estilo de ícone.
- **Raio de borda:** 16dp em cards, 24dp em botões/pills, aplicado consistentemente em 100% do app.
- **Espaçamento:** grade de 8dp (8, 16, 24, 32...) em todos os paddings/margins.

---

## 4. Splash screen

O app demorava com tela branca antes da splash aparecer — isso não pode acontecer. A splash deve:

1. Mostrar uma **tela Compose customizada** com animação completa que aparece imediatamente ao abrir o app: o mascote Mel (`mascote_splash.png`) entra com uma animação de escala + leve quique (overshoot), o nome "PetCare" surge com fade/slide logo abaixo, e a frase "Cuidando dos seus pets com carinho" aparece por último.
2. A navegação para o onboarding (ou direto pro app, se já configurado antes) só deve acontecer quando a animação mínima terminar **E** o carregamento real de dados (verificação de "já viu onboarding" no DataStore) tiver concluído — o que demorar mais define quando a navegação ocorre. Nunca usar um temporizador cego desconectado do carregamento real.

---

## 5. Onboarding (7 telas)

Estrutura (sem fala do mascote em balão de diálogo — texto narrativo/descritivo abaixo da imagem):

1. **Boas-vindas** — `onboarding_1_boasvindas.png`
2. **Meus Pets** — `onboarding_2_meuspets.png`
3. **Diário** — `onboarding_4_fotos.png`
4. **Lembretes** — `onboarding_3_lembretes.png`
5. **Assistente Mel** — `mel_avatar.png`
6. **Escolha de tema** — Sem imagem. Usar um seletor visual moderno e limpo (Claro/Escuro) com ícones de Sol e Lua nativos.
7. **Termos e privacidade** — ver regras específicas abaixo

**Animações do onboarding:**
- Transição entre páginas: slide horizontal + fade.
- Indicador de progresso: pequenas pegadas (não bolinhas genéricas) — a pegada da página atual preenchida em laranja.
- Botão "Próximo": leve efeito de "pressão" ao tocar.
- Botão "Pular" no canto superior direito, presente em todas as páginas exceto a de Termos — pula direto para a tela de Termos, nunca pula a tela de Termos em si.
- Botão/gesto de voltar do sistema: navega para a página anterior do onboarding; só fecha o app se estiver na primeira página.

### Tela de Termos (página 7) — redesenhada

A versão anterior não ficou boa. Nova abordagem:
- Título com ícone (escudo ou similar) e headline curta, tipo "Antes de começar".
- Lista de tópicos curtos com ícone ao lado de cada um (não texto jurídico corrido): dados ficam só no aparelho, notificações só quando criar lembretes, o app mostra anúncios, as dicas do Mel não substituem veterinário.
- Um botão/link discreto "Ler o texto completo" abre o conteúdo integral (Política de Privacidade e Termos de Uso, cada um com seu próprio texto completo — não misturar os dois em um só).
- Área de tópicos com rolagem própria; o checkbox de aceite só habilita depois que o usuário rolar até o fim.
- Adicionar uma pequena animação de entrada nos tópicos (fade + leve deslocamento vertical, aparecendo um de cada vez).
- Botão final: "Aceitar e continuar", desabilitado até o checkbox ser marcado.

---

## 6. Regra global de navegação (vale para as 5 abas)

- **Nenhuma aba deve ter imagens pequenas do mascote decorando o cabeçalho.** Isso já foi tentado antes e ficou com aparência "borrada"/genérica. O cabeçalho de cada aba deve ter só texto (título da tela) sobre o gradiente laranja — sem nenhuma miniatura de ilustração ao lado.
- As ilustrações do mascote (Mel) só aparecem em tamanho grande, nos estados vazios e telas de feedback — nunca como decoração pequena de cabeçalho.
- O botão flutuante do assistente Mel (`mel_avatar_pequeno.png`) deve estar visível em **todas as 5 abas**, sempre na mesma posição (canto inferior direito), e **nunca sobreposto** por outro botão flutuante (como o "+" de adicionar). Quando uma tela tiver os dois botões, empilhe-os verticalmente com espaçamento — o botão do Mel fica acima do botão de ação.

---

## 7. Aba Início

- Header com gradiente laranja: saudação personalizada (com o nome do usuário se preenchido) — mas **a mensagem de saudação não deve ser genérica** tipo só "Olá!". Use algo que mude conforme o horário do dia (ex: "Bom dia, [Nome]!", "Boa tarde", "Boa noite") combinado com uma frase curta e calorosa, não robótica.
- Card de estatísticas (Total de pets / Próxima vacina / Próxima consulta): **mantém o card, mas remove a palavra "Em dia"** — se não houver próximo registro, mostrar apenas "--" sem nenhum selo ou palavra de status.
- Card de dica do Mel: as dicas precisam ser **100% factualmente corretas e verificáveis** (nada de frases genéricas ou inventadas) — baseadas em recomendações reais de cuidado animal (ex: frequência de vacinação por espécie, sinais de desidratação, etc.). Gere um banco de pelo menos 30 dicas verdadeiras por espécie principal (cachorro, gato) e pelo menos 10 para as demais.
- **Não incluir** as seções "Próximos Lembretes" e prévia do "Diário" nesta tela — a Início deve focar em: saudação, estatísticas, dica do Mel, e a lista de pets.
- **Lista de pets na Início:** redesenhar de forma profissional — em vez de avatares circulares soltos numa fileira, considere cards horizontais com foto, nome e um dado rápido (ex: idade), com leve sombra e cantos arredondados, condizente com o resto do app.
- **Estado sem pets:** imagem `vazio_meuspets.png` centralizada (não em canto), mensagem calorosa (não genérica) e um **botão de "Adicionar meu primeiro pet" redesenhado** — nada do botão padrão retangular; usar o mesmo estilo de pill arredondado com ícone de pata que já é o padrão de botão primário do app, com leve animação de destaque (pulso sutil) pra chamar atenção por ser a única ação disponível.
- **Banner de anúncio:** posicionar mais abaixo na tela, com espaçamento maior entre ele e o conteúdo acima, para não parecer que está "grudado"/atrapalhando a leitura do conteúdo.

---

## 8. Aba Meus Pets

- Sem imagem pequena no cabeçalho (regra geral da seção 6).
- **Remover completamente os chips de ordenação** ("Recentes", "Nome A-Z", "Espécie") — não fazem parte desta versão.
- **Contador de pets cadastrados:** substituir o texto atual (que ficou genérico) por um badge mais elegante, integrado visualmente ao título da tela (ex: "Meus Pets" com um badge arredondado ao lado mostrando "3/10", em vez de uma frase separada tipo "0 de 10 cadastrados").
- **Botão de adicionar ("+")**: reposicionar corretamente no canto inferior direito, sem sobrepor o botão do assistente Mel (empilhados, Mel em cima).
- **Estado vazio:** imagem `vazio_meuspets.png` **centralizada na tela** (esse era um bug: a imagem aparecia no canto em vez de centralizada).
- Grade de 2 colunas com cards dos pets cadastrados (foto usando `avatar_pet_padrao.png` como placeholder quando não há foto, nome, espécie/raça, badge de sexo/castração).
- Adicionar animações: stagger (entrada escalonada) dos cards ao carregar a lista, leve compressão ao tocar num card.

---

## 9. Aba Diário

- Sem imagem pequena no cabeçalho.
- **Nunca usar um ícone de livro genérico no lugar da imagem do mascote** — usar `vazio_diario.png` no estado vazio, centralizada.
- **Remover os chips de filtro por categoria** ("Todos", "Memória", "Passeio", etc.) — isso não deveria ter sido criado; o filtro correto (se houver) é por **pet**, não por categoria de momento.
- Timeline vertical com fotos grandes, legenda curta (até 140 caracteres), data, e pet relacionado.
- Botão de compartilhar e editar/excluir em cada entrada.
- **Botão de adicionar ("+")**: reposicionar corretamente, sem sobrepor o botão do Mel.
- **Editor de fotos embutido (Diário e fotos de pet) — versão completa:**
  - Cortar (crop) e girar
  - Filtros prontos: Normal, Vívido, Suave
  - Ajustes manuais com sliders: brilho, contraste, saturação
  - Adesivos temáticos do app: patinhas, coração, moldura estilo polaroid (usando elementos visuais que já existem no pacote de imagens)
  - Texto sobre a imagem, usando a tipografia do app
- Adicionar animações: efeito "polaroid" ao adicionar uma nova entrada (leve rotação inicial que se endireita, com bounce).

---

## 10. Aba Lembretes

- Sem imagem pequena no cabeçalho.
- Lista por data (Hoje / Amanhã / Esta semana / Histórico recolhível), filtro por pet, categorias fixas + personalizado, recorrência (não repete/diária/semanal/mensal), seletor de data e hora com fuso horário correto.
- **Botão de adicionar ("+")**: reposicionar corretamente, sem sobrepor o botão do Mel.
- Notificações locais reais: título/corpo contextual, foto do pet como imagem grande, ícone da categoria tingido com a cor certa, botões de ação "Concluir"/"Adiar 1h", vibração com padrão próprio, agrupamento nativo quando houver várias ao mesmo tempo, reagendamento automático após reiniciar o aparelho (`BroadcastReceiver` para `BOOT_COMPLETED`).
- Editar e excluir lembrete (não só criar).

### Tela "Novo Lembrete" — redesign completo
Mesma atenção da tela "Novo Pet" (seção 11): visual profissional, consistente com a identidade do app, campos bem espaçados, usando os ícones de categoria corretos do pacote de imagens.

---

## 11. Formulário "Novo Pet" / Editar Pet — redesign completo

- Redesenhar visualmente do zero para ficar consistente e profissional — layout limpo, espaçamento em grade de 8dp, tipografia do app.
- Foto do pet: usar `avatar_pet_padrao.png` como placeholder (não o ícone de espécie).
- Seletor de espécie: usar os 7 ícones corretos do pacote (`icone_especie_*`), nunca emoji ou ícone genérico.
- **Campos:** 
    - **Informações Básicas:** Nome*, Foto, Espécie, Sexo, Raça (opcional), Data de nascimento (ou idade aproximada), Peso atual.
    - **Informações Médicas:** Tipo Sanguíneo (opcional), Alergias conhecidas, Condições Crônicas e se é Castrado/Castrada.
    - **Contatos de Emergência (Opcional):** Nome e telefone do Veterinário principal.
    - Microchip (opcional), Observações (personalidade).
- Validações: nome obrigatório, peso numérico positivo, data de nascimento não pode ser no futuro.
- Adicionar animações sutis nas transições entre os campos/seções do formulário.

---

## 12. Sub-abas de saúde do pet (Vacinas, Consultas, Peso, Alimentação, Medicamentos)

- Redesenhar visualmente — atualmente muito simples/genérico.
- Cada estado vazio deve usar a imagem correta do pacote (`vazio_vacinas.png`, `vazio_consultas.png`, `vazio_peso.png`, `vazio_alimentacao.png`, `vazio_medicamentos.png`) — confirmar que estão realmente sendo usadas, não substituídas por ícone genérico.
- **Detalhamento de dados:**
    - **Vacinas:** Nome da vacina, data da aplicação, lote (opcional) e lembrete para a próxima dose.
    - **Medicamentos:** Nome, dosagem (ex: 5mg, 2 gotas), frequência (ex: a cada 8h) e duração do tratamento.
    - **Consultas:** Data, motivo da visita, diagnóstico e orientações do veterinário.
    - **Peso:** Histórico de pesagens com data para acompanhamento da evolução (gráfico de linha, tratando o caso de menos de 2 registros).
    - **Alimentação:** Tipo de ração/comida, quantidade por porção e horários.
- Adicionar animações de entrada nos itens de cada lista.

---

## 13. Exclusão de pet — tela customizada

Em vez do diálogo padrão do Android, criar um modal customizado: imagem `feedback_erro.png` ou pose triste do Mel, mensagem específica com o nome do pet (ex: "Tem certeza que quer remover o Rex e todo o histórico dele? Essa ação não pode ser desfeita."), botão "Cancelar" (neutro) e botão "Remover" (vermelho, mas com a tipografia arredondada do app — não o vermelho genérico do Material Design).

---

## 14. Aba Perfil

- Sem imagem pequena no cabeçalho da própria aba.
- **Alternância de tema:** quando o tema atual é **claro**, o ícone do botão mostra uma **lua**; quando o tema atual é **escuro**, o ícone mostra um **sol**.
- **Exportar backup:** implementar via Storage Access Framework (SAF): pedir ao usuário para escolher uma pasta e guardar a URI com permissão persistente.
- **Mensagem de backup salvo:** "Prontinho! Seus dados estão salvos com segurança 🐾".
- Importar backup: pergunta mesclar ou substituir quando já houver dados.
- **Apagar todos os dados:** confirmação dupla.
- Política de Privacidade, Termos de Uso, Sobre o PetCare: **remover qualquer seção de "Suporte"/contato por e-mail**.
- Revisar a área de nome do usuário: "Como podemos te chamar?", por exemplo.

---

## 15. Assistente Mel — recriar do zero

- Botão flutuante com `mel_avatar_pequeno.png`, presente em **todas as 5 abas**, nunca sobrepondo outro elemento.
- Ao tocar, abre um bottom sheet com o avatar `mel_avatar.png`, nome "Mel — Assistente PetCare", aviso de que é conteúdo informativo.
- **Respostas baseadas em palavras-chave/intenções fixas, 100% offline.** O banco de respostas deve cobrir **todas as funcionalidades do app**.
- Chips de resposta rápida com perguntas comuns.
- Tom com personalidade própria — caloroso, direto.
- Animação de "respiração" sutil no botão flutuante quando ocioso.

---

## 16. Animações — especificação temática completa

Nenhuma animação genérica. Todas devem remeter ao universo pet. Duração máxima de ~400-500ms:
- **Splash:** mascote com quique de entrada.
- **Onboarding:** slide + fade, pegadas no progresso.
- **Navegação:** ícone selecionado faz um "pulo".
- **Botões flutuantes:** respiração sutil; "+" vira "×".
- **Sucesso:** `feedback_sucesso.png` com bounce + partículas de pegadas.
- **Desbloquear:** `feedback_desbloquear.png` com efeito de "caixa abrindo".
- **Lembretes:** check com traço desenhado; swipe com rastro de pegada.
- **Diário:** efeito polaroid.
- **Troca de tema:** transição de cor suave (~300-400ms).
- **Carregamento:** animação do Mel em loop.

---

## 17. Armazenamento e dados

100% local (Room + armazenamento interno para fotos). Sem login, sem conta, sem sincronização em nuvem.
**Correção obrigatória de bug:** ao salvar fotos, NUNCA comprimir para JPEG sem antes desenhar a imagem sobre um fundo branco sólido para evitar fundo preto em imagens transparentes.

---

## 18. Monetização (AdMob)

- IDs de teste durante o desenvolvimento.
- Banner nas abas Início, Meus Pets, Diário e Lembretes.
- Rewarded: ao tentar cadastrar o 11º pet, mostrar tela explicando o limite com botão para assistir anúncio; ao completar, liberar +5 vagas. **Teste esse fluxo de ponta a ponta.**

---

## 19. Textos legais (usar exatamente este conteúdo)

### Política de Privacidade
> **Política de Privacidade do PetCare**
> Última atualização: Julho de 2026
> O PetCare foi criado para funcionar sem exigir cadastro ou conta de usuário. Não coletamos nem armazenamos seus dados pessoais em nenhum servidor.
> **Dados armazenados no seu dispositivo:** nomes, fotos, datas e informações de saúde dos seus pets, as entradas do Diário e os lembretes ficam salvos apenas no seu aparelho.
> **Permissões usadas pelo app:** Câmera, Galeria e Notificações. Cada permissão só é solicitada no momento do uso.
> **Publicidade:** o PetCare exibe anúncios fornecidos pelo Google AdMob. O AdMob pode coletar identificadores de publicidade do dispositivo.
> **Conformidade com a LGPD:** seguimos os princípios da Lei Geral de Proteção de Dados. Você tem controle total sobre seus dados.

### Termos de Uso
> **Termos de Uso do PetCare**
> Última atualização: Julho de 2026
> **1. Natureza do app:** ferramenta de organização. As dicas do Mel **não substituem veterinário**.
> **2. Seus dados:** armazenados localmente. Você é responsável pelos backups.
> **3. Anúncios:** o app oferece recursos gratuitos com limite de pets, expansível via anúncios.
> **4. Propriedade intelectual:** nome, mascote e design são de nossa propriedade.

### Sobre o PetCare
> **Sobre o PetCare**
> PetCare é um aplicativo criado para ajudar tutores a cuidarem melhor dos seus pets — de forma simples, organizada e com carinho. 
> **Versão:** 1.0.0

---

## Observação final
Este documento substitui qualquer versão anterior. Atualize o `PROJECT_CONTEXT.md` a cada etapa. Teste visualmente cada tela antes de marcar como concluída.
