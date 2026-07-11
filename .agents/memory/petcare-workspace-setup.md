---
name: PetCare workspace setup
description: The Replit workspace for the PetCare repo is the real Android/Kotlin project, not the default pnpm scaffold. How to build, verify, and push.
---

O workspace deste projeto (`silvamirandaheitor007-cloud/petcare`) foi sincronizado
para conter o repositório Android real (Kotlin + Jetpack Compose), na raiz —
não é um monorepo pnpm com artifacts. Não recriar a estrutura `lib/`, `artifacts/`,
`package.json` etc. do template padrão do Replit.

**Compilação local não é viável neste ambiente**: não há Android SDK disponível
via `listAvailableModules` (só existe módulo `java-graalvm22.3` para o JDK).
Sem `ANDROID_HOME`/`local.properties`, `./gradlew` falha antes mesmo de compilar
Kotlin. Não vale o tempo de tentar montar um SDK Android manualmente via Nix.

**Como validar mudanças**: revisão manual cuidadosa do código (imports, escopos,
assinaturas) + `git push` para o `origin` usando `GITHUB_TOKEN` como
`x-access-token` na URL remota (não usar a ferramenta `gitPush`/conector, que
falha por falta de credenciais configuradas) + monitorar
`.github/workflows/build.yml` via GitHub REST API
(`GET /repos/.../actions/runs`) até `status: completed`. Esse workflow builda
`assembleDebug` e `assembleRelease`.

**Cuidado com `.replit` e `.gradle/`**: instalar módulos de linguagem (ex. Java
via `installProgrammingLanguage`) reescreve o `.replit` do workspace e pode
apagar seções existentes (ex. bloco `[[ports]]`) — sempre revisar `git diff .replit`
antes de commitar e reverter alterações não intencionais. O diretório `.gradle/`
não deveria estar rastreado (já está no `.gitignore`), mas em algum commit
anterior ficou versionado; ao notar isso, `git rm -r --cached .gradle` antes do
próximo commit.

**Why:** evita perder tempo tentando compilar localmente sem SDK, evita
poluir o repo real com config do editor/sandbox, e mantém o fluxo de validação
real (CI) como fonte de verdade de que o Kotlin compila.

**How to apply:** sempre que fizer mudanças de código Kotlin/Compose neste
projeto, pular a tentativa de build local, revisar manualmente, `git push` com
token, e consultar a API do GitHub Actions para confirmar o resultado antes de
reportar sucesso ao usuário.
