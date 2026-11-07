# syntax=docker/dockerfile:1
# =====================================================================
# Imagem do Painel do Cluster Conecta Vida (visualizador próprio do Swarm)
# Construir a partir da RAIZ do repositório:
#   docker build -t conectavida/painel-cluster:1.0 -f painel-cluster/Dockerfile painel-cluster
#
# Não precisa de build: é um servidor Node.js sem dependências externas,
# que conversa com a API do Docker pelo socket do manager.
# =====================================================================
FROM node:22-alpine
WORKDIR /app

COPY server.js ./
COPY public ./public

ENV PORT=8090 STACK=conectavida
EXPOSE 8090

# Lê o socket do Docker (/var/run/docker.sock), que pertence ao root do host:
# por isso este container roda como root e só pode ficar no manager.
HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8090/healthz || exit 1

CMD ["node", "server.js"]
