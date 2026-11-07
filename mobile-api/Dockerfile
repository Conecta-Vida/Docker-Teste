# syntax=docker/dockerfile:1
# =====================================================================
# Imagem da mobile-api (Aplicativo do cidadão) - build multi-stage
# Construir a partir da RAIZ do repositório:
#   docker build -t conectavida/mobile-api:1.0 -f mobile-api/Dockerfile .
# =====================================================================

# ---------- Estágio 1: build (JDK 21 em Alpine Linux + Gradle Wrapper) ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# 1) Arquivos de build primeiro: a camada de dependências fica em cache
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY core/build.gradle core/
COPY admin-api/build.gradle admin-api/
COPY mobile-api/build.gradle mobile-api/
RUN chmod +x gradlew \
    && ./gradlew --no-daemon -q :mobile-api:dependencies --configuration runtimeClasspath > /dev/null

# 2) Código-fonte: o módulo compartilhado :core e o módulo da API
COPY core/src core/src
COPY mobile-api/src mobile-api/src
RUN ./gradlew --no-daemon :mobile-api:bootJar -x test

# ---------- Estágio 2: runtime (somente JRE 21 em Alpine Linux + app.jar) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário sem privilégios de root
RUN addgroup -S conectavida && adduser -S conectavida -G conectavida
COPY --from=build --chown=conectavida:conectavida /workspace/mobile-api/build/libs/app.jar app.jar
USER conectavida

ENV SERVER_PORT=8081 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8081/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
