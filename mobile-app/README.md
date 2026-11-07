# 📱 Conecta Vida - Aplicativo Móvel Cidadão

<p align="center">
<strong>A interface reativa, portátil e o ponto de contato direto do cidadão com o ecossistema de saúde pública regional. Responsável pela captação de geolocalização ativa, consumo assíncrono de informativos regionalizados, inscrição em mutirões comunitários e interceptação impositiva de alertas epidemiológicos críticos.</strong>
</p>

<p align="center">
<img src="https://img.shields.io/badge/Flutter-02569B?style=for-the-badge&logo=flutter&logoColor=white" alt="Flutter">
<img src="https://img.shields.io/badge/Dart-0175C2?style=for-the-badge&logo=dart&logoColor=white" alt="Dart">
<img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
<img src="https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white" alt="iOS">
<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
<img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4.0.5">
<img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 17">
<img src="https://img.shields.io/badge/Supabase-Pooler-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white" alt="Supabase">
<img src="https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
</p>

<p align="center">
  <a href="https://youtu.be/s7eqyfYcr3E?si=003QmXc9TUCjPm9O" title="Clique para assistir à demonstração">
    <img src="https://img.shields.io/badge/Assista%20à%20Demonstração-FF0000?style=for-the-badge&logo=youtube&logoColor=white" alt="Assista à Demonstração">
  </a>
</p>

---

## 👥 Autores e Instituição
* **Luiz Henrique Gonçalves**
* **Gustavo**
* **Gabriel**
* **Renan**
* **Maycon**

**IFSP - Câmpus Bragança Paulista** *Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas*

---

## 📊 Composição do Código (Languages)

A distribuição das linguagens no repositório reflete a estrutura de uma aplicação Flutter multiplataforma nativa, contendo a lógica em Dart e os runners de compilação específicos:

```text
Dart ───────────────────────────────────────────── 76.9% ███████████████████████████████████░░░░░░░░░░
C++ ────────────────────────────────────────────── 11.4% █████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
CMake ────────────────────────────────────────────  8.8% ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
Swift ────────────────────────────────────────────  1.4% █░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
HTML ─────────────────────────────────────────────  0.7% ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
C ────────────────────────────────────────────────  0.7% ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
Outros ───────────────────────────────────────────  0.1% ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░

```

* **Dart (76.9%):** Core design do ecossistema móvel, contemplando todas as views, controllers, gerência de estados e consumo assíncrono.
* **C++ / CMake (20.2%):** Vinculados às engines de compilação nativa de baixo nível e renderização do Flutter Desktop/Android Shell.
* **Swift (1.4%):** Arquivos de manifesto e delegates nativos do ecossistema iOS.

---

## 📖 Sobre o Módulo Mobile (Aplicativo)

O aplicativo móvel do **Conecta Vida** foi desenvolvido em **Flutter** para entregar uma experiência fluida, reativa e descentralizada de utilidade pública. Ele adota uma arquitetura modularizada dividida em telas (`Screens`), componentes reaproveitáveis (`Widgets`), controladores de estado e lógica (`Controllers`) e modelos de dados estruturados (`Models`).

A aplicação interage de forma assíncrona com os microsserviços da nossa infraestrutura, consumindo dados geográficos locais para reconstruir dinamicamente o feed de notícias do usuário, além de garantir barreira de segurança local contra crises biológicas na comunidade.

---

## Pré-requisitos

Antes de compilar e executar o aplicativo móvel, certifique-se de que o seu ambiente de desenvolvimento possui os seguintes componentes configurados:

* **Flutter SDK (Versão estável mais recente)**: Engine para renderização nativa multiplataforma.
* **Dart SDK**: Linguagem base do projeto.
* **Java JDK 17**: Obrigatório para o funcionamento do pipeline de compilação do Gradle do Android.
* **Android Studio / VS Code**: Ambientes recomendados com as extensões do Flutter instaladas.
* **Emulador Ativo ou Smartphone Físico**: Com a depuração USB ativa para execução dos testes.

---

## Passo 1: Configuração do Ambiente

1. **Clone o repositório mobile** para a sua máquina corporativa:
```bash
git clone <URL_DO_SEU_REPOSITORIO>
cd conecta-vida-mobile

```


2. **Configure as credenciais de infraestrutura**:
Abra o arquivo de serviço localizado em `lib/services/supabase_service.dart` e configure as chaves públicas da sua instância do Supabase para inicialização dos módulos locais:
```dart
String supabaseUrl = "SUA_URL_DO_SUPABASE";
String supabaseAnonKey = "SUA_CHAVE_ANON_DO_SUPABASE";

```



---

## Passo 2: Executando a Aplicação (Modo Simples - Cloud)

Para rodar a aplicação em modo padrão, consumindo a infraestrutura centralizada da nuvem (Supabase e API de Produção), abra o terminal na raiz do projeto mobile e execute:

1. **Baixe os pacotes de dependência** declarados no pubspec:
```bash
flutter pub get

```


2. **Verifique se o seu dispositivo de testes está ativo**:
```bash
flutter devices

```


3. **Inicie o aplicativo**:
```bash
flutter run

```


*(Caso possua mais de um dispositivo conectado, adicione `-d <device_id>` ao final do comando).*

---

## Passo 3: Executando com API Local (Ambiente de Testes)

Para realizar testes avançados ou simular alterações em tempo real feitas na sua API Java local sem depender do servidor do Render, configure a rota base no arquivo do provedor de conexões HTTP:

### 🤖 1. Se estiver testando no Emulador Android

Os emuladores operam em uma sub-rede isolada. Para que ele enxergue o Spring Boot rodando no seu computador (localhost), altere a URL base para o IP de loopback alternativo:

```dart
String apiBaseUrl = "[http://10.0.2.2:8080/api](http://10.0.2.2:8080/api)";

```

### 📱 2. Se estiver testando em um Smartphone Físico (Wi-Fi)

1. Certifique-se de que o celular e o computador estão na mesma rede sem fio.
2. Descubra o IP local da sua máquina (ex: `192.168.1.50`).
3. Altere a rota base do aplicativo:

```dart
String apiBaseUrl = "[http://192.168.1.50:8080/api](http://192.168.1.50:8080/api)";

```

Execute o comando `flutter run` para testar os fluxos integrados localmente.

---

## Passo 4: Testando as Funcionalidades Core

Com o aplicativo em execução no seu dispositivo ou emulador, valide os fluxos projetados de ponta a ponta:

* **Módulo de Entrada:** Splash Animada, Cadastro de Usuário e Recuperação de Conta integrados ao módulo de autenticação.
* **Validação de GPS:** Clique em "Detectar minha cidade" para capturar as coordenadas e injetar dinamicamente o município do usuário no preenchimento geolocalizado.
* **Navegação por Feed Semântico:** Filtre os informativos nas abas *Vacinação, Doações, Urgentes e Eventos* para checar o polimorfismo de dados.
* **Inscrição Relacional (CR7):** Acesse um mutirão público de saúde e clique no ícone de calendário para agendar sua participação na tabela associativa.
* **Pop-up de Intercepção Emergencial:** Simule um login com alertas críticos ativos não lidos na região para atestar o travamento reativo da interface.

---

## Passo 5: Compilando para Produção (Build APK)

Se você precisar gerar o artefato binário final de distribuição do aplicativo Android para submissão ou instalação manual via pendrive, execute na raiz do projeto:

```bash
flutter build apk --release

```

O arquivo de instalação otimizado será compilado e disponibilizado no diretório:

`build/app/outputs/flutter-apk/app-release.apk`

---

© 2026 Conecta à Vida. Sistema homologado para fins acadêmicos. IFSP Bragança Paulista.

```
`build/app/outputs/flutter-apk/app-release.apk`
```


---

© 2026 Conecta à Vida. Sistema homologado para fins acadêmicos. IFSP Bragança Paulista.
