#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/opt/dondok}"
ENV_FILE="${ENV_FILE:-${APP_ROOT}/.env}"
CONFIG_FILE="${CONFIG_FILE:-${APP_ROOT}/config/application-prod.yml}"
IMAGE="${1:?usage: switch-blue-green.sh <docker-image> <commit-sha>}"
DEPLOY_SHA="${2:?usage: switch-blue-green.sh <docker-image> <commit-sha>}"

BLUE_PORT="${BLUE_PORT:-8081}"
GREEN_PORT="${GREEN_PORT:-8082}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"

NGINX_DIR="${APP_ROOT}/nginx"
ACTIVE_UPSTREAM="${NGINX_DIR}/active-upstream.conf"
BLUE_UPSTREAM="${NGINX_DIR}/blue-upstream.conf"
GREEN_UPSTREAM="${NGINX_DIR}/green-upstream.conf"

RELEASE_DIR="${APP_ROOT}/releases"
DEPLOYED_SHA_FILE="${RELEASE_DIR}/deployed-sha.txt"
PREVIOUS_SHA_FILE="${RELEASE_DIR}/previous-sha.txt"

HEALTH_CHECK="${APP_ROOT}/deploy/health-check.sh"
VALIDATE_ENV="${APP_ROOT}/deploy/validate-env.sh"

BACKUP_UPSTREAM=""
NEW_CONTAINER_STARTED=false
SWITCHED=false
NEXT_SLOT=""
NEXT_PORT=""
ACTIVE_SLOT=""

log() {
  echo "[INFO] $*"
}

run_as_root() {
  if [ "$(id -u)" -eq 0 ]; then
    "$@"
  else
    sudo "$@"
  fi
}

reload_nginx() {
  run_as_root nginx -s reload || run_as_root systemctl reload nginx
}

fail() {
  echo "[ERROR] $*" >&2
  exit 1
}

slot_port() {
  case "$1" in
    blue) echo "${BLUE_PORT}" ;;
    green) echo "${GREEN_PORT}" ;;
    *) fail "unknown slot: $1" ;;
  esac
}

detect_active_slot() {
  if [ ! -L "${ACTIVE_UPSTREAM}" ]; then
    echo ""
    return
  fi

  target="$(readlink -f "${ACTIVE_UPSTREAM}")"
  case "${target}" in
    "$(readlink -f "${BLUE_UPSTREAM}")") echo "blue" ;;
    "$(readlink -f "${GREEN_UPSTREAM}")") echo "green" ;;
    *) echo "" ;;
  esac
}

rollback_upstream() {
  if [ -n "${BACKUP_UPSTREAM}" ]; then
    log "rollback upstream to ${BACKUP_UPSTREAM}"
    ln -sfn "${BACKUP_UPSTREAM}" "${ACTIVE_UPSTREAM}"
    if run_as_root nginx -t; then
      reload_nginx || true
    fi
  fi
}

cleanup_on_error() {
  exit_code=$?
  if [ "${exit_code}" -eq 0 ]; then
    return
  fi

  echo "[ERROR] deployment failed, starting cleanup" >&2

  if [ "${SWITCHED}" = true ]; then
    rollback_upstream
  fi

  if [ "${NEW_CONTAINER_STARTED}" = true ] && [ -n "${NEXT_SLOT}" ]; then
    docker rm -f "api-${NEXT_SLOT}" >/dev/null 2>&1 || true
  fi

  exit "${exit_code}"
}

trap cleanup_on_error EXIT

mkdir -p "${APP_ROOT}/logs" "${RELEASE_DIR}"

"${VALIDATE_ENV}" "${ENV_FILE}"

if [ ! -f "${CONFIG_FILE}" ]; then
  fail "application-prod.yml not found: ${CONFIG_FILE}"
fi

ACTIVE_SLOT="$(detect_active_slot)"
case "${ACTIVE_SLOT}" in
  blue) NEXT_SLOT="green" ;;
  green) NEXT_SLOT="blue" ;;
  "") NEXT_SLOT="blue" ;;
  *) fail "invalid active slot: ${ACTIVE_SLOT}" ;;
esac

NEXT_PORT="$(slot_port "${NEXT_SLOT}")"

log "active slot: ${ACTIVE_SLOT:-none}"
log "next slot: ${NEXT_SLOT}"
log "image: ${IMAGE}"

docker pull "${IMAGE}"

log "remove stale inactive container: api-${NEXT_SLOT}"
docker rm -f "api-${NEXT_SLOT}" >/dev/null 2>&1 || true

log "start new container: api-${NEXT_SLOT}"
docker run -d \
  --name "api-${NEXT_SLOT}" \
  --env-file "${ENV_FILE}" \
  -e SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/config/ \
  -e DEPLOYED_SHA="${DEPLOY_SHA}" \
  -p "127.0.0.1:${NEXT_PORT}:${CONTAINER_PORT}" \
  -v "${CONFIG_FILE}:/app/config/application-prod.yml:ro" \
  --restart unless-stopped \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  "${IMAGE}"

NEW_CONTAINER_STARTED=true

READINESS_URL="http://127.0.0.1:${NEXT_PORT}/api/actuator/health/readiness"
"${HEALTH_CHECK}" "${READINESS_URL}" 24 5 3

if [ -L "${ACTIVE_UPSTREAM}" ]; then
  BACKUP_UPSTREAM="$(readlink -f "${ACTIVE_UPSTREAM}")"
else
  BACKUP_UPSTREAM="$(readlink -f "${BLUE_UPSTREAM}")"
fi

log "switch nginx upstream to ${NEXT_SLOT}"
ln -sfn "${NGINX_DIR}/${NEXT_SLOT}-upstream.conf" "${ACTIVE_UPSTREAM}"
SWITCHED=true

run_as_root nginx -t
reload_nginx

ENTRYPOINT_HEALTH_URL="${ENTRYPOINT_HEALTH_URL:-http://127.0.0.1:${NEXT_PORT}/api/health}"
"${HEALTH_CHECK}" "${ENTRYPOINT_HEALTH_URL}" 12 5 3

SMOKE_TEST_URL="${SMOKE_TEST_URL:-}"
if [ -n "${SMOKE_TEST_URL}" ]; then
  "${HEALTH_CHECK}" "${SMOKE_TEST_URL}" 6 5 3
else
  log "S3 upload/download smoke test URL not configured; skipping"
fi

if [ -n "${ACTIVE_SLOT}" ]; then
  log "stop old container: api-${ACTIVE_SLOT}"
  docker rm -f "api-${ACTIVE_SLOT}" >/dev/null 2>&1 || true
fi

if [ -f "${DEPLOYED_SHA_FILE}" ]; then
  cp "${DEPLOYED_SHA_FILE}" "${PREVIOUS_SHA_FILE}"
fi
echo "${DEPLOY_SHA}" > "${DEPLOYED_SHA_FILE}" || {
  echo "[WARN] failed to record deployed sha: ${DEPLOY_SHA}" >&2
}

SWITCHED=false
NEW_CONTAINER_STARTED=false
log "deployment completed: ${DEPLOY_SHA}"
