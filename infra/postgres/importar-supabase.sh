#!/usr/bin/env sh
# =====================================================================
# Copia os dados do Supabase para o Postgres da stack (Docker Swarm).
#
# Uso (Git Bash / Linux), com a stack já rodando:
#   export SUPABASE_DB_URL='postgresql://postgres.SEU_PROJETO@aws-1-sa-east-1.pooler.supabase.com:5432/postgres'
#   export SUPABASE_DB_PASSWORD='sua senha'     # separada da URL: aceita @ # / % etc.
#   ./infra/postgres/importar-supabase.sh
#
# - Use a porta 5432 (Session pooler) do Supabase, não a 6543.
# - SUBSTITUI os dados de demonstração do Postgres local pelos do Supabase.
# - Não altera nada no Supabase (só lê).
# - Para os logins antigos funcionarem, faça o deploy com CONECTA_API_SALT igual
#   ao app.security.salt que o backend usava com o Supabase.
# =====================================================================
set -eu

: "${SUPABASE_DB_URL:?Defina SUPABASE_DB_URL (veja o comentário no topo do script)}"
PG_USER=${POSTGRES_USER:-conectavida}
PG_DB=${POSTGRES_DB:-conectavida}
IMG=postgres:17-alpine

# Tabelas na ordem das chaves estrangeiras
TABELAS="usuarios instituicoes_saude comunicacoes usuarios_campanhas logs_atividade"

PG=${PG_CONTAINER:-$(docker ps -q -f name=conectavida_postgres | head -1)}
[ -n "$PG" ] || { echo "Container do Postgres da stack não encontrado. A stack está rodando?"; exit 1; }

local_sql()  { docker exec -i "$PG" psql -X -q -v ON_ERROR_STOP=1 -U "$PG_USER" -d "$PG_DB" "$@"; }
# A senha vai por PGPASSWORD (variável de ambiente), sem precisar escapar na URL
remoto_sql() { PGPASSWORD="${SUPABASE_DB_PASSWORD:-}" docker run --rm -i -e PGPASSWORD -e PGSSLMODE=require "$IMG" psql -X -q -v ON_ERROR_STOP=1 "$SUPABASE_DB_URL" "$@"; }

colunas() { # $1 = local|remoto, $2 = tabela -> uma coluna por linha
  q="select column_name from information_schema.columns where table_schema='public' and table_name='$2' order by ordinal_position"
  if [ "$1" = local ]; then local_sql -tAc "$q"; else remoto_sql -tAc "$q"; fi
}

echo "==> Testando conexão com o Supabase..."
if ! remoto_sql -tAc "select 'ok'" >/dev/null; then
  echo
  echo "Falha ao conectar. Confira:"
  echo "  - usuário no formato postgres.SEU_PROJETO (não só 'postgres')"
  echo "  - porta 5432 (Session pooler)"
  echo "  - senha atual do banco (Supabase > Project Settings > Database);"
  echo "    passe-a em SUPABASE_DB_PASSWORD, fora da URL"
  exit 1
fi

echo "==> Limpando os dados de demonstração do Postgres local"
local_sql -c "TRUNCATE $(echo $TABELAS | sed 's/ /, /g') RESTART IDENTITY CASCADE"

for t in $TABELAS; do
  if [ -z "$(remoto_sql -tAc "select to_regclass('public.$t')")" ]; then
    echo "   - $t: não existe no Supabase, pulando"; continue
  fi
  colunas local "$t" > /tmp/cv_local_cols
  comuns=$(colunas remoto "$t" | grep -Fxf /tmp/cv_local_cols | sed 's/.*/"&"/' | paste -sd, -)
  [ -n "$comuns" ] || { echo "   - $t: nenhuma coluna em comum, pulando"; continue; }

  remoto_sql -c "\\copy (SELECT $comuns FROM public.$t) TO STDOUT" \
    | local_sql -c "\\copy public.$t($comuns) FROM STDIN"
  n=$(local_sql -tAc "select count(*) from public.$t")
  echo "   - $t: $n registros"

  # Ajusta o contador do id para continuar depois do maior id importado
  local_sql -tAc "select setval(pg_get_serial_sequence('public.$t','id'), coalesce(max(id),1), max(id) is not null) from public.$t" >/dev/null
done
rm -f /tmp/cv_local_cols


echo "==> Importação concluída."
