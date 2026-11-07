#!/usr/bin/env sh
# =====================================================================
# Roteiro automatizado do seminário: Docker Swarm + Docker-in-Docker
#
#   ./infra/swarm/seminario.sh subir     -> cria o cluster e faz o deploy
#   ./infra/swarm/seminario.sh escalar   -> demonstração de escalabilidade
#   ./infra/swarm/seminario.sh status    -> mostra nós, serviços e réplicas
#   ./infra/swarm/seminario.sh derrubar  -> remove stack, workers e registry
#
# Rode SEMPRE a partir da raiz do repositório.
# Funciona em Linux e no Docker Desktop (use o Git Bash ou WSL no Windows).
# =====================================================================
set -eu

STACK=conectavida
REGISTRY=127.0.0.1:5000
TAG=${TAG:-1.0}
WORKERS="worker-1 worker-2"

passo() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }

ip_do_manager() {
  # IP do manager visto pelos containers DinD (gateway da rede bridge padrão)
  docker network inspect bridge -f '{{(index .IPAM.Config 0).Gateway}}'
}

subir() {
  passo "Passo 0: verificando Docker e Compose"
  docker --version
  docker compose version

  MANAGER_IP=$(ip_do_manager)
  if [ "$(docker info -f '{{.Swarm.LocalNodeState}}')" != "active" ]; then
    passo "Ativando o modo Swarm (manager em $MANAGER_IP)"
    docker swarm init --advertise-addr "$MANAGER_IP"
  else
    echo "Swarm já está ativo neste host."
  fi

  passo "Tarefa 1: build das imagens (multi-stage, Alpine Linux)"
  if [ "${PULAR_BUILD:-0}" = "1" ]; then
    echo "PULAR_BUILD=1: usando as imagens já construídas."
  else
    docker build -t conectavida/admin-api:"$TAG" -f admin-api/Dockerfile .
    docker build -t conectavida/mobile-api:"$TAG" -f mobile-api/Dockerfile .
  fi

  passo "Registry local do cluster (para os workers baixarem as imagens)"
  if ! docker service inspect registry >/dev/null 2>&1; then
    docker service create --name registry --publish published=5000,target=5000 \
      --constraint node.role==manager registry:2
  fi
  i=0
  until wget -qO- "http://$REGISTRY/v2/" >/dev/null 2>&1 || curl -fs "http://$REGISTRY/v2/" >/dev/null 2>&1; do
    i=$((i+1)); [ $i -gt 30 ] && { echo "Registry não respondeu"; exit 1; }; sleep 2
  done
  for img in admin-api mobile-api; do
    docker tag conectavida/$img:"$TAG" $REGISTRY/conectavida/$img:"$TAG"
    docker push $REGISTRY/conectavida/$img:"$TAG"
  done

  passo "Tarefa 3.1: criando os workers com Docker-in-Docker"
  for w in $WORKERS; do
    if ! docker container inspect "$w" >/dev/null 2>&1; then
      docker run -d --privileged --name "$w" --hostname "$w" docker:dind
    fi
  done
  for w in $WORKERS; do
    i=0
    until docker exec "$w" docker info >/dev/null 2>&1; do
      i=$((i+1)); [ $i -gt 30 ] && { echo "$w não iniciou"; exit 1; }; sleep 2
    done
  done

  passo "Tarefa 3.2: conectando os workers ao manager"
  TOKEN=$(docker swarm join-token -q worker)
  for w in $WORKERS; do
    if [ "$(docker exec "$w" docker info -f '{{.Swarm.LocalNodeState}}')" != "active" ]; then
      docker exec "$w" docker swarm join --token "$TOKEN" "$MANAGER_IP:2377"
    fi
  done
  docker node ls

  passo "Tarefa 3.3: deploy da stack"
  REGISTRY=$REGISTRY TAG=$TAG docker stack deploy -c docker-compose.yml "$STACK"

  passo "Pronto!"
  echo "  Visualizer ......... http://localhost:8085"
  echo "  admin-api .......... http://localhost:8080/actuator/health"
  echo "  mobile-api ......... http://localhost:8081/actuator/health"
  echo "  RabbitMQ (painel) .. http://localhost:15672  (conectavida / conectavida)"
  echo "As APIs levam ~1 minuto para ficarem saudáveis. Acompanhe com: $0 status"
}

escalar() {
  passo "Tarefa 3.5: escalabilidade ao vivo (observe o Visualizer)"
  docker service scale ${STACK}_mobile-api=6
  docker service scale ${STACK}_admin-api=4
  docker service scale ${STACK}_mobile-api=2
  docker service ls
}

status() {
  docker node ls
  docker stack services "$STACK"
  docker stack ps "$STACK" --format 'table {{.Name}}\t{{.Node}}\t{{.CurrentState}}' --filter desired-state=running
}

derrubar() {
  passo "Removendo stack, registry e workers"
  docker stack rm "$STACK" || true
  docker service rm registry 2>/dev/null || true
  for w in $WORKERS; do
    docker exec "$w" docker swarm leave --force 2>/dev/null || true
    docker rm -f "$w" 2>/dev/null || true
  done
  for id in $(docker node ls -q -f role=worker 2>/dev/null); do docker node rm --force "$id" || true; done
  echo "Para sair do modo Swarm no manager: docker swarm leave --force"
}

case "${1:-}" in
  subir) subir ;;
  escalar) escalar ;;
  status) status ;;
  derrubar) derrubar ;;
  *) echo "Uso: $0 {subir|escalar|status|derrubar}"; exit 1 ;;
esac
