# 🐾 PetIngle

**PetIngle** é um aplicativo Android para tutores de animais de estimação que querem organizar a vida dos seus pets com carinho — tudo salvo localmente no aparelho, sem login, sem conta, sem nuvem.

<p align="center">
  <img src="petingle/app/src/main/res/drawable/mascote_splash.png" width="180" alt="Mascote PetIngle"/>
</p>

---

## ✨ Funcionalidades

- **Meus Pets** — Cadastre quantos pets quiser com foto, espécie, raça, sexo, data de nascimento, informações médicas e contatos de emergência.
- **Diário** — Registre momentos especiais com fotos e legendas numa timeline visual estilo polaroid.
- **Lembretes** — Crie lembretes de vacinas, consultas, banho, medicação e muito mais. Notificações locais com botões de "Concluir" e "Adiar 1h".
- **Saúde** — Sub-abas por pet para registrar vacinas, consultas, histórico de peso (com gráfico), alimentação e medicamentos.
- **Backup** — Exporte e importe seus dados manualmente, direto no seu dispositivo.

---

## 📱 Stack técnica

| Tecnologia | Uso |
|---|---|
| Kotlin + Jetpack Compose | UI 100% declarativa |
| Room (SQLite) | Banco de dados local |
| Hilt | Injeção de dependência |
| Coil | Carregamento de imagens |
| DataStore | Preferências do usuário |
| WorkManager | Tarefas em background |
| Material 3 | Design system |
| Fonte Nunito | Tipografia do app |

---

## 🏗️ Como compilar

### Pré-requisitos

- Android Studio Ladybug ou superior
- JDK 17
- Android SDK (minSdk 24, targetSdk 35)

### Passos

```bash
git clone https://github.com/silvamirandaheitor9-creator/petingle.git
cd petingle/petingle
./gradlew assembleDebug
```

O APK de debug estará em `app/build/outputs/apk/debug/app-debug.apk`.

### Build via GitHub Actions

A cada push na branch `main`, o workflow [`.github/workflows/build.yml`](.github/workflows/build.yml) compila automaticamente os APKs de debug e release. Os artefatos ficam disponíveis na aba **Actions** do repositório.

---

## 📂 Estrutura do projeto

```
petingle/
├── app/
│   └── src/main/
│       ├── java/br/com/petingle/
│       │   ├── data/          # Room, DAOs, DataStore, Notificações
│       │   ├── di/            # Módulos Hilt
│       │   ├── ui/
│       │   │   ├── screen/    # Telas Compose (Splash, Onboarding, Main, etc.)
│       │   │   ├── theme/     # Cores, tipografia, formas
│       │   │   └── viewmodel/ # ViewModels
│       │   └── util/          # Utilitários (datas, dicas)
│       └── res/
│           ├── drawable/      # Ícones e ilustrações do mascote
│           ├── font/          # Fonte Nunito (TTF local)
│           └── mipmap-*/      # Ícone adaptável do app
└── docs/
    └── privacy-policy.html
```

---

## 🎨 Identidade visual

- **Mascote:** Mel, filhote de cachorro bicolor marrom/branco, estilo chibi
- **Cores:** fundo `#FFF8F3`, laranja primário `#FF7A3D`, gradiente `#FF9152 → #FF5E3A`
- **Tema escuro:** fundo `#1E1A17`, cards `#2B2420`, laranja `#FF8C42`
- **Tipografia:** Nunito (Google Fonts, carregada como TTF local)
- **Raio de borda:** 16dp em cards, 24dp em botões

---

## 🔒 Privacidade

O PetIngle **não coleta dados**, não exige conta e não usa servidores externos. Todas as informações ficam exclusivamente no seu aparelho. Veja a [Política de Privacidade](docs/privacy-policy.html) completa.

---

## 📄 Licença

© 2026 PetIngle. Todos os direitos reservados.  
O nome, mascote e design são propriedade exclusiva dos criadores do app.
