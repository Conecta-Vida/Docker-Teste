# syntax=docker/dockerfile:1
# =====================================================================
# Imagem da admin-api (Painel administrativo) - build multi-stage
# Construir a partir da RAIZ do repositório:
#   docker build -t conectavida/admin-api:1.0 -f admin-api/Dockerfile .
# =====================================================================

# ---------- Estágio 1: build (JDK 17 em Alpine Linux + Gradle Wrapper) ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

# 1) Arquivos de build primeiro: a camada de dependências fica em cache
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY core/build.gradle core/
COPY admin-api/build.gradle admin-api/
COPY mobile-api/build.gradle mobile-api/
RUN chmod +x gradlew \
    && ./gradlew --no-daemon -q :admin-api:dependencies --configuration runtimeClasspath > /dev/null

# 2) Código-fonte: o módulo compartilhado :core e o módulo da API
COPY core/src core/src
COPY admin-api/src admin-api/src
RUN ./gradlew --no-daemon :admin-api:bootJar -x test

# ---------- Estágio 2: runtime (somente JRE 17 em Alpine Linux + app.jar) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuário sem privilégios de root
RUN addgroup -S conectavida && adduser -S conectavida -G conectavida
COPY --from=build --chown=conectavida:conectavida /workspace/admin-api/build/libs/app.jar app.jar
USER conectavida

ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
