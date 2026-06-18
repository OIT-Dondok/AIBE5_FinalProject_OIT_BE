#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/opt/dondok}"
APP_NETWORK="${APP_NETWORK:-dondok-network}"
MONITORING_DIR="${MONITORING_DIR:-${APP_ROOT}/monitoring}"

if ! docker network inspect "${APP_NETWORK}" >/dev/null 2>&1; then
  docker network create "${APP_NETWORK}" >/dev/null
fi

for slot in blue green; do
  container_name="api-${slot}"
  if [ "$(docker inspect -f '{{.State.Running}}' "${container_name}" 2>/dev/null || true)" = "true" ]; then
    if ! docker inspect -f '{{json .NetworkSettings.Networks}}' "${container_name}" | grep -q "\"${APP_NETWORK}\""; then
      docker network connect "${APP_NETWORK}" "${container_name}"
    fi
  fi
done

if [ ! -f "${MONITORING_DIR}/.env" ]; then
  echo "[ERROR] monitoring env file not found: ${MONITORING_DIR}/.env" >&2
  echo "[ERROR] copy ${MONITORING_DIR}/.env.example to ${MONITORING_DIR}/.env and set GRAFANA_ADMIN_PASSWORD" >&2
  exit 1
fi

docker compose \
  --env-file "${MONITORING_DIR}/.env" \
  -f "${MONITORING_DIR}/compose.yaml" \
  up -d
