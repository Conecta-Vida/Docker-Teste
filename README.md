# Conecta à Vida: monorepo (backend + painel web + app + Docker Swarm)

<p align="center">
<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
<img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
<img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React">
<img src="https://img.shields.io/badge/Flutter-3-02569B?style=for-the-badge&logo=flutter&logoColor=white" alt="Flutter">
<img src="https://img.shields.io/badge/Docker%20Swarm-Alpine-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker Swarm">
</p>

Plataforma que centraliza informações de saúde pública: notícias, alertas epidemiológicos, campanhas de vacinação e doação de sangue, e a rede de unidades de saúde. O gestor publica pelo **painel web** e o cidadão recebe tudo no **aplicativo**.

## 👥 Autores
Gabriel Dias Ribeiro · Gustavo Barros Tenório · Luiz Henrique Gonçalves · Renan Valença Bueno Rodrigues · Maycon Cabral da Silva

**IFSP - Câmpus Bragança Paulista**, Tecnologia em Análise e Desenvolvimento de Sistemas

## 📁 Estrutura

```
.
├── core/          # módulo Java compartilhado (entidades, repositórios, serviços)
├── admin-api/     # API do painel administrativo  -> porta 8080  (+ Dockerfile)
├── mobile-api/    # API do aplicativo do cidadão  -> porta 8081  (+ Dockerfile)
├── admin-web/     # painel administrativo V2 (React + Vite), servido por Nginx (+ Dockerfile)
├── mobile-app/    # aplicativo Flutter do cidadão (+ Dockerfile: versão Web na porta 8089)
├── infra/
│   ├── postgres/init/   # schema e dados de demonstração do banco
│   └── swarm/seminario.sh  # roteiro automatizado do seminário (Swarm + DinD)
├── docker-compose.yml   # stack do Docker Swarm
└── documentacao/        # especificação, guia do Swarm, vídeo e imagens
```

`core`, `admin-api` e `mobile-api` formam **um único build Gradle** (`settings.gradle`).

## 🐳 Rodar tudo no Docker Swarm (3 nós)

```bash
./infra/swarm/seminario.sh subir
```

Depois abra o **Visualizer** em http://localhost:8085, o **painel admin** em http://localhost:8088 (`admin@conectavida.com` / `admin123`) e o **app do cidadão** em http://localhost:8089 (`cidadao@conectavida.com` / `cidadao123`).

🎤 **Para apresentar**, rode o roteiro guiado (Enter avança; mostra o que falar e executa cada demonstração). Cola de 1 página: [documentacao/ROTEIRO_APRESENTACAO.md](documentacao/ROTEIRO_APRESENTACAO.md)

```bash
./infra/swarm/seminario.sh apresentar
```

Só a demonstração de escalabilidade:

```bash
./infra/swarm/seminario.sh escalar
```

📗 **Manual passo a passo (rodar e apresentar):** [documentacao/MANUAL.md](documentacao/MANUAL.md) · [PDF](documentacao/Manual-Conecta-Vida-Docker.pdf)

📘 Guia completo, com cada Dockerfile e o compose explicados linha a linha: **[documentacao/DOCKER_SWARM.md](documentacao/DOCKER_SWARM.md)**
🎬 Vídeos e fotos do projeto rodando: **[Parte K do manual](documentacao/MANUAL.md#parte-k-fotos-e-vídeos-plano-b-e-slides)** (pasta `documentacao/midia/`)

## 💻 Desenvolvimento local (sem Swarm)

**1. Banco** em container:

```bash
docker run -d --name cv-postgres -p 5432:5432 -e POSTGRES_DB=conectavida -e POSTGRES_USER=conectavida -e POSTGRES_PASSWORD=conectavida -v "$PWD/infra/postgres/init:/docker-entrypoint-initdb.d:ro" postgres:17-alpine
```

**2. APIs** (JDK 21), cada uma num terminal:

```bash
./gradlew :admin-api:bootRun
```

```bash
./gradlew :mobile-api:bootRun
```


**3. Painel web** (Node 18+). O Vite encaminha `/api` para `localhost:8080`:

```bash
cd admin-web && npm install && npm run dev
```

**4. App Flutter.** Por padrão ele usa `http://10.0.2.2:8081/api` no emulador Android e `http://localhost:8081/api` no Chrome. Para um celular físico, informe o IP do PC:

```bash
cd mobile-app && flutter run --dart-define=API_BASE_URL=http://192.168.0.10:8081/api
```

## 🔑 Configuração
Variáveis disponíveis em [`.env.example`](.env.example). Os principais endpoints estão descritos em [`documentacao/`](documentacao/).

© 2026 Conecta à Vida. Projeto acadêmico do IFSP Bragança Paulista.
