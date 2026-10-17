#!/usr/bin/env bash
set -euo pipefail

SUFIXO="${SUFIXO:-rm565824}"
MEU_IP="${1:?Informe o seu IP público. Exemplo: ./liberar-meu-ip.sh 200.100.50.25}"

az sql server firewall-rule create \
  --resource-group rg-vetpet \
  --server "sql-vetpet-${SUFIXO}" \
  --name MeuComputador \
  --start-ip-address "$MEU_IP" \
  --end-ip-address "$MEU_IP"
