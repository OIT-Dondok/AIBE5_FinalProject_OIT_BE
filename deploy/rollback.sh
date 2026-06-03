#!/usr/bin/env bash
set -euo pipefail

# Nginx 트래픽을 지정한 slot으로 수동 전환한다.
# 사용 예: ./rollback.sh blue 또는 ./rollback.sh green

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

# blue/green upstream만 rollback 대상으로 허용한다.
case "${TARGET_SLOT}" in
  blue|green) ;;
  *) echo "[ERROR] target slot must be blue or green" >&2; exit 1 ;;
esac

# active upstream symlink를 변경하고, Nginx 설정이 유효할 때만 reload한다.
ln -sfn "${NGINX_DIR}/${TARGET_SLOT}-upstream.conf" "${ACTIVE_UPSTREAM}"
run_as_root nginx -t
reload_nginx

echo "[INFO] rollback switched active upstream to ${TARGET_SLOT}"
