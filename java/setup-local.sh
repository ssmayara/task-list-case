#!/usr/bin/bash


# ===== CONFIGURAÇÕES =====
CONTAINER_NAME="mariadb"
DB_NAME="mydatabase"
DB_USER="root"
DB_PASS="verysecret"

SQL_FILE="create-table.sql"

echo "📦 Importando tabelas a partir de:"
echo "   $SQL_FILE"
echo ""

if [ ! -f "$SQL_FILE" ]; then
  echo "❌ Arquivo não encontrado nesse caminho!"
  exit 1
fi

docker exec -i "$CONTAINER_NAME" \
  mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"

if [ $? -eq 0 ]; then
  echo "✅ Importação concluída com sucesso!"
else
  echo "❌ Erro ao importar tabelas."
fi