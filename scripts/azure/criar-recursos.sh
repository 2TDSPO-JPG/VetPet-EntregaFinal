#!/usr/bin/env bash
set -euo pipefail

SUFIXO="${SUFIXO:-rm565824}"
LOCAL="${LOCAL:-brazilsouth}"
GRUPO="rg-vetpet"
SQL_SERVIDOR="sql-vetpet-${SUFIXO}"
SQL_BANCO="vetpet"
SQL_ADMIN="${SQL_ADMIN:-clyvoadmin}"
PLANO="plan-vetpet"
APP_API="vetpet-api-${SUFIXO}"
APP_NOTIFICACAO="vetpet-notificacao-${SUFIXO}"

if [ -z "${SQL_SENHA:-}" ]; then
  read -r -s -p "Senha do administrador do Azure SQL: " SQL_SENHA
  echo
fi

az group create --name "$GRUPO" --location "$LOCAL"

az sql server create \
  --resource-group "$GRUPO" \
  --name "$SQL_SERVIDOR" \
  --location "$LOCAL" \
  --admin-user "$SQL_ADMIN" \
  --admin-password "$SQL_SENHA"

az sql db create \
  --resource-group "$GRUPO" \
  --server "$SQL_SERVIDOR" \
  --name "$SQL_BANCO" \
  --edition GeneralPurpose \
  --family Gen5 \
  --capacity 2 \
  --compute-model Serverless \
  --auto-pause-delay 60 \
  --use-free-limit \
  --free-limit-exhaustion-behavior AutoPause \
  --backup-storage-redundancy Local

az sql server firewall-rule create \
  --resource-group "$GRUPO" \
  --server "$SQL_SERVIDOR" \
  --name PermitirServicosAzure \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

az appservice plan create \
  --resource-group "$GRUPO" \
  --name "$PLANO" \
  --location "$LOCAL" \
  --is-linux \
  --sku B1

az webapp create \
  --resource-group "$GRUPO" \
  --plan "$PLANO" \
  --name "$APP_NOTIFICACAO" \
  --runtime "JAVA:21-java21"

az webapp create \
  --resource-group "$GRUPO" \
  --plan "$PLANO" \
  --name "$APP_API" \
  --runtime "JAVA:21-java21"

for APP in "$APP_API" "$APP_NOTIFICACAO"; do
  az webapp config set \
    --resource-group "$GRUPO" \
    --name "$APP" \
    --always-on true \
    --startup-file "java -jar /home/site/wwwroot/app.jar --server.port=80"
  az webapp update --resource-group "$GRUPO" --name "$APP" --https-only true
done

az webapp config appsettings set \
  --resource-group "$GRUPO" \
  --name "$APP_API" \
  --settings \
    SPRING_PROFILES_ACTIVE=prod \
    SEED_HABILITADO=true \
    VETPET_ADMIN_EMAIL=admin@vetpet.com \
    NOTIFICACAO_URL="https://${APP_NOTIFICACAO}.azurewebsites.net"

az webapp config appsettings set \
  --resource-group "$GRUPO" \
  --name "$APP_NOTIFICACAO" \
  --settings \
    MAIL_HOST=smtp.gmail.com \
    MAIL_PORT=587

echo
echo "DB_URL=jdbc:sqlserver://${SQL_SERVIDOR}.database.windows.net:1433;database=${SQL_BANCO};encrypt=true;trustServerCertificate=false;loginTimeout=30"
echo "DB_USERNAME=${SQL_ADMIN}"
echo "API:         https://${APP_API}.azurewebsites.net"
echo "Notificação: https://${APP_NOTIFICACAO}.azurewebsites.net"
