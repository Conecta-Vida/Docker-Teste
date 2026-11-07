#!/usr/bin/env sh
# =====================================================================
# Roteiro automatizado do seminário: Docker Swarm + Docker-in-Docker
#
#   ./infra/swarm/seminario.sh subir        -> cria o cluster e faz o deploy
#   ./infra/swarm/seminario.sh apresentar   -> ROTEIRO GUIADO do seminário (Enter avança)
#   ./infra/swarm/seminario.sh replicas mobile-api 6
#                                           -> muda as réplicas pelo docker-compose.yml
#   ./infra/swarm/seminario.sh escalar      -> escala com docker service scale (6/4/2)
#   ./infra/swarm/seminario.sh falha        -> desliga o worker-2 (simula queda de máquina)
#   ./infra/swarm/seminario.sh recuperar    -> religa o worker-2
#   ./infra/swarm/seminario.sh status       -> mostra nós, serviços e réplicas
#   ./infra/swarm/seminario.sh derrubar [--apagar-dados] -> remove tudo
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
fala()  { printf '\033[1;33m   💬 %s\033[0m\n' "$*"; }        # o que dizer na apresentação
cmd()   { printf '\033[1;32m   $ %s\033[0m\n' "$*"; "$@"; }  # mostra e executa o comando
pausa() { printf '\n\033[2m   [Enter para continuar]\033[0m'; read -r _; }

# Réplicas escolhidas ficam salvas aqui, para cada deploy repetir as anteriores
REPLICAS_ENV=infra/swarm/.replicas.env

deploy() {
  [ -f "$REPLICAS_ENV" ] && . "./$REPLICAS_ENV"
  export ADMIN_API_REPLICAS="${ADMIN_API_REPLICAS:-2}" MOBILE_API_REPLICAS="${MOBILE_API_REPLICAS:-3}"
  export ADMIN_WEB_REPLICAS="${ADMIN_WEB_REPLICAS:-2}" MOBILE_WEB_REPLICAS="${MOBILE_WEB_REPLICAS:-2}"
  REGISTRY=$REGISTRY TAG=$TAG docker stack deploy -c docker-compose.yml "$STACK"
}

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
    # Usa o endereço com que o manager já foi anunciado
    MANAGER_IP=$(docker info -f '{{.Swarm.NodeAddr}}')
    echo "Swarm já está ativo neste host (manager em $MANAGER_IP)."
    echo "Se os workers não conseguirem entrar: docker swarm leave --force e rode o script de novo."
  fi

  passo "Tarefa 1: build das 4 imagens (admin-api, mobile-api, admin-web, mobile-web)"
  if [ "${PULAR_BUILD:-0}" = "1" ]; then
    echo "PULAR_BUILD=1: usando as imagens já construídas."
  else
    docker build -t conectavida/admin-api:"$TAG" -f admin-api/Dockerfile .
    docker build -t conectavida/mobile-api:"$TAG" -f mobile-api/Dockerfile .
    docker build -t conectavida/admin-web:"$TAG" -f admin-web/Dockerfile admin-web
    # O app Flutter demora mais na primeira vez (baixa o SDK do Flutter, ~1,5 GB)
    docker build -t conectavida/mobile-web:"$TAG" -f mobile-app/Dockerfile mobile-app
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
  for img in admin-api mobile-api admin-web mobile-web; do
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
  passo "Etiquetando os nós (aparece no Visualizer)"
  docker node update --label-add papel=manager --label-add hospeda=infra "$(docker node ls -q -f role=manager | head -1)" >/dev/null
  n=1
  for w in $WORKERS; do
    docker node update --label-add papel=worker --label-add zona=zona-$n "$w" >/dev/null
    n=$((n+1))
  done
  docker node ls

  passo "Tarefa 3.3: deploy da stack"
  rm -f "$REPLICAS_ENV"   # começa com os números padrão do docker-compose.yml
  deploy

  passo "Pronto!"
  echo "  Visualizer ......... http://localhost:8085"
  echo "  Painel admin (web) . http://localhost:8088  (admin@conectavida.com / admin123)"
  echo "  App do cidadão ..... http://localhost:8089  (cidadao@conectavida.com / cidadao123)"
  echo "  admin-api .......... http://localhost:8080/actuator/health"
  echo "  mobile-api ......... http://localhost:8081/actuator/health"
  echo "  Swagger ............ http://localhost:8080/swagger-ui.html e http://localhost:8081/swagger-ui.html"
  echo "As APIs levam ~1 minuto para ficarem saudáveis. Acompanhe com: $0 status"
}

escalar() {
  passo "Tarefa 3.5: escalabilidade ao vivo (observe o Visualizer)"
  cmd docker service scale ${STACK}_mobile-api=6
  cmd docker service scale ${STACK}_admin-api=4
  cmd docker service scale ${STACK}_mobile-api=2
  docker service ls
}

# Infraestrutura como código: muda o número no deploy do docker-compose.yml
replicas() {
  svc=${2:-}; n=${3:-}
  case "$svc" in
    admin-api) var=ADMIN_API_REPLICAS ;; mobile-api) var=MOBILE_API_REPLICAS ;;
    admin-web) var=ADMIN_WEB_REPLICAS ;; mobile-web) var=MOBILE_WEB_REPLICAS ;;
    *) echo "Uso: $0 replicas {admin-api|mobile-api|admin-web|mobile-web} NUMERO"; exit 1 ;;
  esac
  case "$n" in ''|*[!0-9]*) echo "Informe o número de réplicas. Ex.: $0 replicas $svc 6"; exit 1 ;; esac
  touch "$REPLICAS_ENV"
  grep -v "^$var=" "$REPLICAS_ENV" > "$REPLICAS_ENV.tmp" || true
  echo "$var=$n" >> "$REPLICAS_ENV.tmp"; mv "$REPLICAS_ENV.tmp" "$REPLICAS_ENV"
  passo "docker-compose.yml: $svc com replicas: $n  (variável $var)"
  printf '\033[1;32m   $ %s=%s docker stack deploy -c docker-compose.yml %s\033[0m\n' "$var" "$n" "$STACK"
  deploy >/dev/null
  sleep 3
  docker service ls --filter name=${STACK}_$svc
}

falha() {
  passo "Simulando a queda do worker-2"
  cmd docker stop worker-2
  echo "   Em ~30s o worker-2 fica vermelho (Down) no Visualizer e as réplicas dele"
  echo "   são recriadas nos outros nós. Volte com: $0 recuperar"
}

recuperar() {
  passo "Religando o worker-2"
  cmd docker start worker-2
  echo "   O nó volta a ficar verde. As réplicas não voltam sozinhas: o Swarm só move"
  echo "   containers quando precisa (novos deploys/escalas já usam o worker-2)."
}

abrir() { # abre uma URL no navegador (Windows, macOS ou Linux)
  if command -v cmd.exe >/dev/null 2>&1; then cmd.exe /c start "" "$1" >/dev/null 2>&1
  elif command -v open >/dev/null 2>&1; then open "$1"
  elif command -v xdg-open >/dev/null 2>&1; then xdg-open "$1" >/dev/null 2>&1
  else echo "   Abra: $1"; fi
}

# Roteiro guiado: mostra o que dizer e executa cada demonstração
apresentar() {
  passo "1/7 - O cluster: 3 nós (1 manager + 2 workers Docker-in-Docker)"
  fala "O manager coordena o cluster; os workers executam os containers."
  fala "Os workers são containers docker:dind que simulam outras máquinas."
  cmd docker node ls
  abrir http://localhost:8085
  pausa

  passo "2/7 - O que roda em cada nó (Visualizer)"
  fala "Manager: postgres (banco), visualizer e registry - o banco guarda DADOS e fica preso"
  fala "ao manager por 'constraint: node.role == manager'."
  fala "Workers: APIs e sites, sem estado - o Swarm espalha as cópias entre os nós."
  fala "Mesma cor no Visualizer = réplicas do mesmo serviço."
  cmd docker stack services $STACK
  pausa

  passo "3/7 - O sistema funcionando"
  fala "Painel do gestor (React) e app do cidadão (Flutter) rodando como serviços do cluster."
  abrir http://localhost:8088
  abrir http://localhost:8089
  echo "   Painel: admin@conectavida.com / admin123   |   App: cidadao@conectavida.com / cidadao123"
  pausa

  passo "4/7 - Infraestrutura como código: réplicas pelo docker-compose.yml"
  fala "Tudo está descrito no docker-compose.yml. Para escalar, mudamos o número de réplicas"
  fala "e reaplicamos o arquivo: o Swarm cria as cópias que faltam. Olhem o Visualizer."
  grep -n "replicas: \${MOBILE_API" docker-compose.yml || true
  replicas x mobile-api 6
  pausa
  fala "Agora diminuindo: o Swarm remove as cópias que sobraram."
  replicas x mobile-api 2
  pausa

  passo "5/7 - Escalando pelo terminal (docker service scale)"
  fala "Também dá para escalar na hora, sem mexer no arquivo."
  cmd docker service scale ${STACK}_admin-api=4
  pausa
  cmd docker service scale ${STACK}_admin-api=2
  pausa

  passo "6/7 - Alta disponibilidade: uma máquina cai"
  fala "Vou desligar o worker-2. O Swarm percebe e recria as réplicas nos outros nós."
  falha
  pausa
  recuperar
  pausa

  passo "7/7 - Fim"
  fala "Resumo: 6 serviços, 3 nós, rede overlay, réplicas declaradas no docker-compose.yml,"
  fala "imagens multi-stage em Alpine, escalabilidade e recuperação automática."
  rm -f "$REPLICAS_ENV"
  deploy >/dev/null
  echo "   (Réplicas voltaram aos valores padrão do docker-compose.yml.)"
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

  # Espera os containers da stack pararem (os volumes só podem ser apagados depois)
  i=0
  while [ -n "$(docker ps -q -f label=com.docker.stack.namespace=$STACK)" ] && [ $i -lt 60 ]; do
    i=$((i+1)); sleep 2
  done

  if [ "${2:-}" = "--apagar-dados" ]; then
    passo "Apagando os dados do Postgres (volume)"
    docker volume rm ${STACK}_postgres-data 2>/dev/null || true
  fi
  echo "Pronto. Para sair do modo Swarm no manager: docker swarm leave --force"
}

case "${1:-}" in
  subir) subir ;;
  apresentar) apresentar ;;
  replicas) replicas "$@" ;;
  escalar) escalar ;;
  falha) falha ;;
  recuperar) recuperar ;;
  status) status ;;
  derrubar) derrubar "$@" ;;
  *) echo "Uso: $0 {subir|apresentar|replicas SERVICO N|escalar|falha|recuperar|status|derrubar [--apagar-dados]}"; exit 1 ;;
esac
