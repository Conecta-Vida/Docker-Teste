# Roteiro do seminário (cola de 1 página)

## Antes de começar (5 a 15 min antes)
1. Abra o **Docker Desktop** e espere ficar pronto.
2. No **Git Bash**, na pasta do projeto:

```bash
./infra/swarm/seminario.sh subir
```

3. Espere cerca de 1 minuto e confira se tudo está `N/N`:

```bash
./infra/swarm/seminario.sh status
```

## Durante a apresentação: um comando só

```bash
./infra/swarm/seminario.sh apresentar
```

O script abre o Visualizer, o painel e o app, mostra **o que falar** (em amarelo) e executa cada demonstração. Aperte **Enter** para avançar.

| Passo | O que aparece | O que falar |
|---|---|---|
| 1 | `docker node ls` + Visualizer | "São 3 nós: o manager coordena e os 2 workers (containers Docker-in-Docker) executam os containers." |
| 2 | Serviços por nó | "Banco, fila e registry ficam presos ao manager porque guardam dados. APIs e sites não guardam estado, então o Swarm espalha as cópias. Mesma cor = mesmo serviço." |
| 3 | Painel (8088) e app (8089) | "Este é o sistema rodando dentro do cluster: o gestor publica e o cidadão recebe." |
| 4 | `replicas: 6` e depois `2` | "**Infraestrutura como código**: as réplicas estão declaradas no docker-compose.yml. Mudamos o número, reaplicamos o arquivo e o Swarm cria ou remove as cópias." |
| 5 | `docker service scale` | "Também dá para escalar na hora pelo terminal." |
| 6 | `docker stop worker-2` | "Se uma máquina cai, o Swarm recria as réplicas dela nos outros nós e o sistema continua no ar." |
| 7 | Resumo | "8 serviços, 3 nós, rede overlay, imagens multi-stage em Alpine, escalabilidade e recuperação automática." |

## Comandos avulsos (se quiser fazer na mão)

| Para | Comando |
|---|---|
| Mudar réplicas pelo compose | `./infra/swarm/seminario.sh replicas mobile-api 6` |
| Escalar pelo terminal | `docker service scale conectavida_mobile-api=6` |
| Derrubar uma máquina | `./infra/swarm/seminario.sh falha` |
| Religar a máquina | `./infra/swarm/seminario.sh recuperar` |
| Ver tudo | `./infra/swarm/seminario.sh status` |

## Endereços

| O quê | Endereço | Login |
|---|---|---|
| Visualizer | http://localhost:8085 | (sem login) |
| Painel admin | http://localhost:8088 | admin@conectavida.com / admin123 |
| App do cidadão | http://localhost:8089 | cidadao@conectavida.com / cidadao123 |
| RabbitMQ | http://localhost:15672 | conectavida / conectavida |

## Se algo der errado
- **Alguma caixa não fica verde:** espere 1 minuto; as APIs reiniciam sozinhas até o banco subir.
- **Página não abre:** confira com `./infra/swarm/seminario.sh status` se as réplicas estão `N/N`.
- **Recomeçar do zero:** rode `./infra/swarm/seminario.sh derrubar --apagar-dados` e depois `./infra/swarm/seminario.sh subir`.

Explicação detalhada dos Dockerfiles e do compose: [DOCKER_SWARM.md](DOCKER_SWARM.md).
