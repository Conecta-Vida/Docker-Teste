# syntax=docker/dockerfile:1
# =====================================================================
# Imagem do admin-web (painel React + Vite) - build multi-stage
# Construir a partir da RAIZ do repositório:
#   docker build -t conectavida/admin-web:1.0 -f admin-web/Dockerfile admin-web
# =====================================================================

# ---------- Estágio 1: build (Node.js 22 em Alpine Linux) ----------
FROM node:22-alpine AS build
WORKDIR /app

# Dependências primeiro (camada em cache enquanto package*.json não mudar)
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# Código-fonte e build de produção (TypeScript + Vite -> /app/dist)
COPY . .
ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ---------- Estágio 2: runtime (Nginx em Alpine Linux) ----------
FROM nginx:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD wget -qO- http://127.0.0.1/healthz || exit 1
