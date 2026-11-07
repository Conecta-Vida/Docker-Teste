# syntax=docker/dockerfile:1
# =====================================================================
# Imagem do mobile-web: o app Flutter do cidadão compilado para a Web,
# para usar no navegador (PC ou celular) sem instalar o Flutter.
# Construir a partir da RAIZ do repositório:
#   docker build -t conectavida/mobile-web:1.0 -f mobile-app/Dockerfile mobile-app
# =====================================================================

# ---------- Estágio 1: build com o SDK do Flutter ----------
# O Flutter não roda em Alpine (precisa de glibc), por isso o build usa a
# imagem oficial da comunidade (Debian). O runtime abaixo é Alpine.
FROM ghcr.io/cirruslabs/flutter:3.44.0 AS build
WORKDIR /app

# Dependências primeiro (camada em cache enquanto o pubspec não mudar)
COPY pubspec.yaml pubspec.lock ./
RUN flutter pub get

# Código e build Web. "/api" = mesmo endereço do site; o Nginx encaminha
# para o serviço mobile-api do Swarm.
COPY . .
ARG API_BASE_URL=/api
RUN flutter build web --release --dart-define=API_BASE_URL=$API_BASE_URL

# ---------- Estágio 2: runtime (Nginx em Alpine Linux) ----------
FROM nginx:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/build/web /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD wget -qO- http://127.0.0.1/healthz || exit 1
