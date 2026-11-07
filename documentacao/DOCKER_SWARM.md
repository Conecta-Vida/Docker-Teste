# Conecta Vida no Docker Swarm

Guia da atividade de **conteinerização e orquestração**: as imagens, a stack, o cluster de 3 nós e o roteiro do seminário.

![Visualizer com 3 nós](midia/visualizer-3-nos.png)

🎬 **Vídeo:** [`midia/demo-conectavida-swarm.mp4`](midia/demo-conectavida-swarm.mp4) mostra o cluster e o escalonamento ao vivo (gravado com a versão anterior do painel).

---

## 1. Arquitetura

```
                 ┌──────────────── rede overlay: conectavida-net ────────────────┐
 Navegador ─8088─▶ admin-web (Nginx) ──/api──▶ admin-api (8080) ──┐              │
                                                                  ├─▶ postgres   │
 Navegador ─8089─▶ mobile-web (Nginx) ─/api──▶ mobile-api (8081) ─┘    (5432)    │
 App Flutter (celular/emulador) ─────8081────▶ mobile-api                        │
 Navegador ─8085─▶ visualizer (lê o docker.sock do manager)                      │
                 └────────────────────────────────────────────────────────────────┘
```

| # | Serviço | Imagem | Porta | Réplicas | Onde roda |
|---|---------|--------|-------|----------|-----------|
| 1 | `postgres` | `postgres:17-alpine` | 5432 | 1 | manager |
| 2 | `admin-api` | `conectavida/admin-api` (nossa, Java 21) | 8080 | 2 | qualquer nó |
| 3 | `mobile-api` | `conectavida/mobile-api` (nossa, Java 21) | 8081 | 3 | qualquer nó |
| 4 | `visualizer` | `dockersamples/visualizer:stable` | 8085 | 1 | manager |
| 5 | `admin-web` | `conectavida/admin-web` (painel admin V2, React) | 8088 | 2 | qualquer nó |
| 6 | `mobile-web` | `conectavida/mobile-web` (app Flutter no navegador) | 8089 | 2 | qualquer nó |

O banco roda **localmente, dentro do cluster** (serviço `postgres`); o Supabase só é usado se vocês importarem os dados dele (seção 5).
O backend é o **conecta-vida-backend** (Spring Boot 3.2, Java 21, Swagger). RabbitMQ e Redis foram descartados do projeto.

Todas as imagens rodam em **Alpine Linux**.

### Por que dividir em `admin-api` e `mobile-api`?

O backend é **um único projeto Gradle com três módulos**:

| Módulo | O que contém |
|--------|--------------|
| `core` | Entidades JPA, repositórios e serviços compartilhados (senha com salt, notificações, dashboard) |
| `admin-api` | Rotas do painel do gestor: login de administrador, dashboard, alertas, campanhas, instituições, usuários, logs e relatórios |
| `mobile-api` | Rotas do app do cidadão: cadastro e login, perfil, feed de comunicações, inscrição em campanhas e registro do celular |

Assim cada API escala de forma independente. No dia de uma campanha de vacinação, por exemplo, só a `mobile-api` precisa de mais réplicas.

---

## 2. Os Dockerfiles explicados

### 2.1 `admin-api/Dockerfile` (backend Java)

O `mobile-api/Dockerfile` é idêntico. Só mudam o módulo (`:mobile-api`) e a porta (8081).

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
```
**Estágio 1 (build).** Imagem com o **JDK 21** completo (compilador) sobre Alpine. O apelido `AS build` permite copiar arquivos deste estágio mais tarde.

```dockerfile
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY core/build.gradle core/
COPY admin-api/build.gradle admin-api/
COPY mobile-api/build.gradle mobile-api/
RUN chmod +x gradlew \
    && ./gradlew --no-daemon -q :admin-api:dependencies --configuration runtimeClasspath > /dev/null
```
Copia **só os arquivos de build** e baixa as dependências. O Docker guarda esta camada em cache: enquanto os `build.gradle` não mudarem, um novo build não baixa o Spring de novo. O `settings.gradle` declara os três módulos, então os três `build.gradle` precisam existir.

```dockerfile
COPY core/src core/src
COPY admin-api/src admin-api/src
RUN ./gradlew --no-daemon :admin-api:bootJar -x test
```
Agora copia o código-fonte do **módulo compartilhado `:core`** e o da API, e gera o JAR executável `admin-api/build/libs/app.jar`. O Gradle compila o `:core` antes, porque a `admin-api` depende dele.

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S conectavida && adduser -S conectavida -G conectavida
COPY --from=build --chown=conectavida:conectavida /workspace/admin-api/build/libs/app.jar app.jar
USER conectavida
```
**Estágio 2 (runtime).** Começa do zero com uma imagem que tem **só o JRE** (sem compilador, Gradle ou código-fonte). Do estágio de build vem apenas o `app.jar`. A aplicação roda com um **usuário sem privilégios**, e não como root, o que é uma boa prática de segurança.

```dockerfile
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
```
- `MaxRAMPercentage=75`: a JVM respeita o limite de memória do container (512 MB no compose).
- `ExitOnOutOfMemoryError`: se a memória acabar, o processo morre e o Swarm sobe outro.
- `EXPOSE`: documenta a porta usada.

```dockerfile
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```
- `HEALTHCHECK`: consulta o **Spring Actuator**. O Swarm só considera a réplica pronta quando ela responde, e a substitui se ela travar.
- `exec`: o Java vira o processo principal (PID 1) e recebe o sinal de parada do Docker corretamente.

> **Resultado do multi-stage:** a imagem final tem só o JRE e o JAR (sem JDK, Gradle ou cache de dependências) e roda Alpine Linux v3.24.

### 2.2 `mobile-app/Dockerfile` (app Flutter no navegador)

Igual ao do painel, mas o estágio 1 usa a imagem `ghcr.io/cirruslabs/flutter:3.44.0` (o SDK do Flutter não roda em Alpine, precisa de glibc) e executa `flutter build web --dart-define=API_BASE_URL=/api`. O estágio 2 é o mesmo Nginx Alpine, encaminhando `/api` para a `mobile-api`. Assim o app pode ser aberto no PC ou no celular sem instalar o Flutter.

### 2.3 `admin-web/Dockerfile` (front-end React)

```dockerfile
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
```
**Estágio 1.** Node 22 sobre Alpine. Primeiro copia só os `package*.json` e instala as dependências com `npm ci`, que respeita exatamente o lock. Essa camada fica em cache.

```dockerfile
COPY . .
ARG VITE_API_URL=
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build
```
Copia o código e gera o build de produção (TypeScript + Vite) na pasta `dist/`. `VITE_API_URL` vazio significa "mesmo endereço do site": as rotas do admin V2 já começam com `/api`, e quem as encaminha é o Nginx do próprio container.

```dockerfile
FROM nginx:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK ... CMD wget -qO- http://127.0.0.1/healthz || exit 1
```
**Estágio 2.** Só o **Nginx** com os arquivos estáticos, sem Node nem `node_modules`. A imagem final tem cerca de **74 MB**.

O arquivo [`admin-web/nginx/default.conf`](../admin-web/nginx/default.conf) faz três coisas:
1. **`location /api/` → `proxy_pass http://admin-api:8080`**: encaminha as chamadas para o serviço `admin-api`. O nome é resolvido pelo DNS interno do Swarm (`resolver 127.0.0.11`), que distribui entre as réplicas.
2. **`try_files ... /index.html`**: garante que rotas do React como `/alertas` funcionem ao recarregar a página.
3. **`/assets/` com cache de 1 ano**: os arquivos têm hash no nome, então podem ser cacheados.

### 2.4 Imagens prontas

| Serviço | Por que esta imagem |
|---------|---------------------|
| `postgres:17-alpine` | Banco oficial, versão Alpine (menor) |
| `dockersamples/visualizer:stable` | Painel gráfico do livro *Descomplicando o Docker* |

---

## 3. O `docker-compose.yml` explicado

```yaml
x-api-env: &api-env
  DB_URL: jdbc:postgresql://postgres:5432/conectavida
  DB_USERNAME: conectavida
  CONECTA_API_SALT: ...
```
**Âncora YAML**: as variáveis comuns às duas APIs ficam num lugar só e são reaproveitadas com `<<: *api-env`. O host `postgres` é o **nome do serviço**, resolvido pelo DNS da rede overlay.

```yaml
  postgres:
    image: postgres:17-alpine
    volumes:
      - postgres-data:/var/lib/postgresql/data
    configs:
      - source: db-schema
        target: /docker-entrypoint-initdb.d/01-schema.sql
      - source: db-seed
        target: /docker-entrypoint-initdb.d/02-seed.sql
    deploy:
      placement:
        constraints: [node.role == manager]
```
- `volumes`: os dados sobrevivem a reinícios.
- `configs`: **Swarm configs** entregam os scripts SQL ao container. Na primeira inicialização o Postgres cria as tabelas (`01-schema.sql`) e os dados de demonstração (`02-seed.sql`). Diferente de um bind mount, um config funciona em qualquer nó.
- `constraints: [node.role == manager]`: o banco fica **sempre no manager**, junto do seu volume.

```yaml
  admin-api:
    image: ${REGISTRY:-127.0.0.1:5000}/conectavida/admin-api:${TAG:-1.0}
    depends_on: [postgres]
    deploy:
      replicas: ${ADMIN_API_REPLICAS:-2}
      update_config:
        parallelism: 1
        order: start-first
      restart_policy:
        condition: on-failure
        delay: 10s
      resources:
        limits:   { memory: 512M }
        reservations: { memory: 128M }
```
- `image`: as imagens vêm do **registry local do cluster** (`127.0.0.1:5000`). Os workers não têm as imagens que você construiu na sua máquina, então elas precisam estar num registry que todos os nós alcancem.
- `depends_on`: o Swarm **ignora** essa opção, que fica só como documentação. Quem resolve a ordem de subida é a `restart_policy`: se a API subir antes do banco, ela falha e o Swarm tenta de novo depois de 10 s.
- `replicas`: quantidade de réplicas. Vem de uma variável com valor padrão (`${MOBILE_API_REPLICAS:-3}`): sem nada, sobe 3; com `MOBILE_API_REPLICAS=6 docker stack deploy ...`, sobe 6. É assim que o `seminario.sh replicas` muda o número **pelo arquivo** (infraestrutura como código).
- `update_config`: *rolling update*, uma réplica por vez, subindo a nova antes de derrubar a antiga (`start-first`). Assim a atualização não tem downtime. Cada réplica nova é observada por 30 s (`monitor`), e se falhar o Swarm **volta sozinho para a versão anterior** (`failure_action: rollback`). Para demonstrar: `TAG=1.1 docker stack deploy ...` com uma imagem nova.
- `resources`: o `limit` impede uma réplica de consumir toda a RAM do nó. A `reservation` é a memória que o scheduler garante ao escolher o nó.

```yaml
  visualizer:
    image: dockersamples/visualizer:stable
    ports: ["8085:8080"]
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
    deploy:
      placement:
        constraints: [node.role == manager]
```
O Visualizer lê o estado do cluster pelo **socket do Docker** (somente leitura). Só o manager conhece o cluster inteiro, por isso a constraint.

```yaml
networks:
  conectavida-net:
    driver: overlay
    attachable: true
```
A rede **overlay** liga containers que estão em **nós diferentes** como se estivessem na mesma rede local. Com `attachable: true` dá para conectar um container avulso a ela para testes.

**`deploy.labels`** (`conectavida.camada: backend-mobile` etc.) classificam cada serviço por camada, e o **script do seminário etiqueta os nós** (`papel=manager/worker`, `zona=zona-1/zona-2`). Essas etiquetas aparecem no Visualizer e diferenciam o nosso projeto.

---

## 4. Como rodar (passo a passo)

### Passo 0: pré-requisitos
- **Docker Desktop** (Windows/Mac) ou **Docker Engine** (Linux).
- No Windows, use o **Git Bash** para rodar os scripts `.sh`.

```bash
docker --version
```

```bash
docker compose version
```

### Opção A: tudo automático (recomendado)

Na **raiz do repositório**:

```bash
./infra/swarm/seminario.sh subir
```

O script executa todo o roteiro do professor:
1. Ativa o Swarm (`docker swarm init`).
2. **Tarefa 1:** constrói as 4 imagens (`admin-api`, `mobile-api`, `admin-web`, `mobile-web`).
3. Cria o registry local (`127.0.0.1:5000`) e envia as imagens para ele.
4. **Tarefa 3.1:** cria `worker-1` e `worker-2` com `docker:dind`.
5. **Tarefa 3.2:** pega o token (`docker swarm join-token worker`) e conecta os dois workers.
6. Etiqueta os nós (as etiquetas aparecem no Visualizer).
7. **Tarefa 3.3:** `docker stack deploy -c docker-compose.yml conectavida`.

Depois de cerca de 1 minuto:

| O quê | Endereço | Login |
|-------|----------|-------|
| **Visualizer** | http://localhost:8085 | (sem login) |
| **Painel admin V2** | http://localhost:8088 | `admin@conectavida.com` / `admin123` |
| **App do cidadão** | http://localhost:8089 | `cidadao@conectavida.com` / `cidadao123` |
| Swagger das APIs | http://localhost:8080/swagger-ui.html e http://localhost:8081/swagger-ui.html | (sem login) |

**Apresentação guiada** (mostra o que falar e executa cada demonstração; Enter avança). Cola de 1 página: [ROTEIRO_APRESENTACAO.md](ROTEIRO_APRESENTACAO.md).

```bash
./infra/swarm/seminario.sh apresentar
```

Outros comandos:

| Para | Comando |
|---|---|
| Réplicas pelo compose (infraestrutura como código) | `./infra/swarm/seminario.sh replicas mobile-api 6` |
| Escalar pelo terminal (6/4/2) | `./infra/swarm/seminario.sh escalar` |
| Simular a queda de uma máquina | `./infra/swarm/seminario.sh falha` |
| Religar a máquina | `./infra/swarm/seminario.sh recuperar` |
| Ver nós, serviços e réplicas | `./infra/swarm/seminario.sh status` |
| Desmontar tudo e apagar o banco | `./infra/swarm/seminario.sh derrubar --apagar-dados` |

### Opção B: comando por comando (para explicar cada etapa)

```bash
docker swarm init
```

```bash
docker build -t conectavida/admin-api:1.0 -f admin-api/Dockerfile .
```

```bash
docker build -t conectavida/mobile-api:1.0 -f mobile-api/Dockerfile .
```

```bash
docker build -t conectavida/admin-web:1.0 -f admin-web/Dockerfile admin-web
```

```bash
docker build -t conectavida/mobile-web:1.0 -f mobile-app/Dockerfile mobile-app
```

Registry local e envio das imagens:

```bash
docker service create --name registry --publish published=5000,target=5000 --constraint node.role==manager registry:2
```

```bash
for img in admin-api mobile-api admin-web mobile-web; do docker tag conectavida/$img:1.0 127.0.0.1:5000/conectavida/$img:1.0; docker push 127.0.0.1:5000/conectavida/$img:1.0; done
```

Workers Docker-in-Docker:

```bash
docker run -d --privileged --name worker-1 --hostname worker-1 docker:dind
```

```bash
docker run -d --privileged --name worker-2 --hostname worker-2 docker:dind
```

Conectar os workers. O IP do manager, visto de dentro dos containers, é o gateway da rede bridge (normalmente `172.17.0.1`):

```bash
docker swarm join-token worker
```

```bash
docker exec worker-1 docker swarm join --token <TOKEN> 172.17.0.1:2377
```

```bash
docker exec worker-2 docker swarm join --token <TOKEN> 172.17.0.1:2377
```

```bash
docker node ls
```

Deploy:

```bash
docker stack deploy -c docker-compose.yml conectavida
```

> Se o Swarm já tiver sido iniciado com outro IP, os workers DinD podem não alcançá-lo. Nesse caso, use `docker swarm leave --force` e depois `docker swarm init --advertise-addr 172.17.0.1`. O script já faz isso automaticamente.

### Variáveis de ambiente
Os valores padrão servem para o laboratório. Para trocar senhas, copie `.env.example` para `.env`, edite e exporte antes do deploy (`set -a; . ./.env; set +a`). O `docker stack deploy` **não** lê o `.env` sozinho.

---

## 5. Usar os dados reais do Supabase

O Postgres da stack começa com dados de demonstração. Para trazer os dados do Supabase (só leitura, nada é alterado lá):

1. Faça o deploy com o **mesmo salt** que o backend usava com o Supabase, senão os logins antigos não funcionam. O valor é o `app.security.salt` dos `application.properties` antigos.

```bash
export CONECTA_API_SALT='o-mesmo-salt-de-antes'
```

2. Com a stack rodando, no Git Bash:

```bash
export SUPABASE_DB_URL='postgresql://postgres.SEU_PROJETO@aws-1-sa-east-1.pooler.supabase.com:5432/postgres'
```

```bash
export SUPABASE_DB_PASSWORD='senha-atual-do-banco'
```

```bash
./infra/postgres/importar-supabase.sh
```

Use a porta **5432** (Session pooler) e o usuário no formato `postgres.SEU_PROJETO`. A senha vai separada, então pode ter `@`, `#` ou `/` sem problema. O script copia usuários, instituições, comunicações, inscrições e logs e ajusta os contadores de id.

## 6. Testes realizados

Ambiente de teste: 1 manager + 2 workers `docker:dind`.

| Teste | Resultado |
|-------|-----------|
| Build das 4 imagens (multi-stage, Alpine) | ✅ |
| Stack distribuída nos 3 nós | ✅ |
| Postgres inicializado pelos configs (schema + dados) e APIs com `ddl-auto=validate` | ✅ |
| Login do admin pelo painel V2 (admin-web → Nginx → admin-api → Postgres) | ✅ |
| App: cadastro, login, feed e inscrição em campanha pela mobile-api | ✅ |
| Importação: cópia de outro Postgres para o da stack, ignorando colunas extras | ✅ |
| Rolling update 2.0 → 2.1 | ✅ |
| Comando `replicas` (gera o deploy com o número pedido) | ✅ (lógica testada; deploy real a conferir no ensaio) |

---

## 7. Problemas comuns

| Sintoma | Causa / solução |
|---------|-----------------|
| Réplica em `Rejected` com "No such image" | A imagem não está no registry. Rode o `docker push` para `127.0.0.1:5000`. |
| Workers não entram no Swarm | O manager foi iniciado com outro IP. Rode `docker swarm leave --force` e depois `docker swarm init --advertise-addr 172.17.0.1`. |
| API reiniciando nos primeiros segundos | É normal: ela subiu antes do Postgres. A `restart_policy` resolve sozinha. |
| Mudou o `02-seed.sql` e nada aconteceu | Os scripts só rodam com o banco vazio. Rode `./infra/swarm/seminario.sh derrubar --apagar-dados` e suba de novo. |
| Painel diz "Não foi possível conectar à API" | Espere ~1 minuto após o deploy e confira com `./infra/swarm/seminario.sh status`. |
| Importação do Supabase: "password authentication failed" | Usuário deve ser `postgres.SEU_PROJETO`, porta 5432, senha atual em `SUPABASE_DB_PASSWORD`. |

## Limitações conhecidas
- Notificações push são **simuladas**: o alerta é registrado no log da API (o Firebase foi removido do backend novo).
- O login não gera um JWT assinado e as rotas não exigem autenticação; isso vem do projeto original e está fora do escopo desta atividade.

## Segurança
- As senhas do compose são **de laboratório**. Em produção, use `.env` ou *Docker secrets*.
- O Visualizer acessa o socket do Docker: não o exponha publicamente.
- Credenciais que estiveram em commits (Supabase, Gmail, CloudAMQP) devem ser **trocadas**. Removê-las do arquivo atual não as apaga do histórico do Git.
