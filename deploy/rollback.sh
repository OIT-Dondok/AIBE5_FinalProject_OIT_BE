#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/opt/dondok}"
NGINX_DIR="${APP_ROOT}/nginx"
ACTIVE_UPSTREAM="${NGINX_DIR}/active-upstream.conf"
TARGET_SLOT="${1:?usage: rollback.sh <blue|green>}"

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

case "${TARGET_SLOT}" in
  blue|green) ;;
  *) echo "[ERROR] target slot must be blue or green" >&2; exit 1 ;;
esac

ln -sfn "${NGINX_DIR}/${TARGET_SLOT}-upstream.conf" "${ACTIVE_UPSTREAM}"
run_as_root nginx -t
reload_nginx

echo "[INFO] rollback switched active upstream to ${TARGET_SLOT}"
