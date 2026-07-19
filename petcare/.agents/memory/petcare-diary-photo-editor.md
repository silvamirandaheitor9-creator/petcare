---
name: PetCare Diary photo editor
description: Scope decisions and fallbacks for the embedded photo editor in the Diário tab (crop/rotate, filters, adjustments, stickers, text) — useful if extending or debugging it later.
---

O editor de fotos do Diário (SPEC 9.8-9.11) foi implementado com estas
decisões deliberadas de escopo, úteis para não repetir a mesma investigação:

- **Sem câmera**: o "+" abre só a galeria (`ActivityResultContracts.PickVisualMedia`).
  Câmera ficou fora do escopo para não inflar a tarefa.
- **Crop = pan & zoom dentro de moldura quadrada fixa** (estilo Instagram/WhatsApp),
  não retângulo redimensionável por handles. Rotação só em incrementos de 90°.
- **Sem lib de terceiros de crop/edição de imagem** — tudo implementado nativamente
  com `android.graphics.Canvas`/`Bitmap`/`ColorMatrix`.
- **Adesivos (patinha/coração) são desenhados via `android.graphics.Path`, não
  imagens** — o pacote de assets do projeto não tem nenhum arquivo de patinha,
  coração ou moldura polaroid. A mesma função de desenho é reaproveitada na
  prévia (Compose, via `nativeCanvas`) e na exportação final, para garantir
  paridade visual. Moldura polaroid = borda branca desenhada, não asset.
- **Sem handles de redimensionar/rotacionar adesivos/texto** — só arrastar para
  posicionar e um botão remover no selecionado. O SPEC só pede a existência dos
  adesivos, não manipulação avançada.
- **Fonte do texto exportado**: a Nunito do app é uma Google Font baixável
  (`GoogleFont.Provider`), sem `.ttf` estático no projeto. Fora do Compose (no
  `Canvas`/`Paint` usado para gerar o JPEG final) não há forma confiável de
  buscá-la de forma síncrona, então o texto exportado usa
  `Typeface.create("sans-serif-medium", Typeface.BOLD)` como fallback. A prévia
  em tela usa a Nunito real. Isso é uma limitação conhecida e documentada no
  `PROJECT_CONTEXT.md`.
- **Regra do fundo branco (SPEC 17.3)**: sempre desenhar a imagem final sobre um
  fundo branco sólido antes de comprimir em JPEG, para nunca vazar fundo preto
  em fotos com transparência. Implementado em `flattenOnWhiteBackground`,
  chamado dentro de `saveDiaryPhotoJpeg` — replicar essa mesma chamada em
  qualquer novo fluxo de salvar foto (ex.: futura foto de perfil do pet).

**Why:** essas decisões vieram de restrições reais do projeto (sem assets
prontos, sem SDK Android local para testar, fonte assíncrona) — não são
arbitrárias, então vale preservá-las em vez de redescobrir na próxima sessão.

**How to apply:** ao estender o editor (ex. adicionar novos adesivos, permitir
redimensionar, trocar fonte) ou ao debugar diferenças entre a prévia e a foto
salva, checar esta lista primeiro.
