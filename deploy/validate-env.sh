#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${1:-/opt/dondok/.env}"

if [ ! -f "${ENV_FILE}" ]; then
  echo "[ERROR] env file not found: ${ENV_FILE}" >&2
  exit 1
fi

get_env_value() {
  awk -v key="$1" '
    /^[[:space:]]*($|#)/ { next }
    {
      line = $0
      sub(/\r$/, "", line)
      split(line, parts, "=")
      env_key = parts[1]
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", env_key)

      if (env_key == key) {
        sub(/^[^=]*=/, "", line)
        print line
        found = 1
        exit
      }
    }
    END {
      if (!found) {
        exit 1
      }
    }
  ' "${ENV_FILE}" || true
}

required_vars=(
  SPRING_PROFILES_ACTIVE
  MYSQL_URL
  MYSQL_USER
  MYSQL_PASSWORD
  REDIS_HOST
  REDIS_PORT
  AWS_REGION
  AWS_S3_BUCKET
  JWT_SECRET
  CORS_ALLOWED_ORIGINS
  COOKIE_SECURE
  COOKIE_SAME_SITE
)

missing=()
for var_name in "${required_vars[@]}"; do
  value="$(get_env_value "${var_name}")"
  if [ -z "${value}" ]; then
    missing+=("${var_name}")
  fi
done

if [ "${#missing[@]}" -gt 0 ]; then
  echo "[ERROR] missing required environment variables:" >&2
  printf '  - %s\n' "${missing[@]}" >&2
  exit 1
fi

SPRING_PROFILES_ACTIVE_VALUE="$(get_env_value SPRING_PROFILES_ACTIVE)"
COOKIE_SECURE_VALUE="$(get_env_value COOKIE_SECURE)"
COOKIE_SAME_SITE_VALUE="$(get_env_value COOKIE_SAME_SITE)"

if [ "${SPRING_PROFILES_ACTIVE_VALUE}" != "prod" ]; then
  echo "[ERROR] SPRING_PROFILES_ACTIVE must be prod" >&2
  exit 1
fi

if [ "${COOKIE_SAME_SITE_VALUE}" = "None" ] && [ "${COOKIE_SECURE_VALUE}" != "true" ]; then
  echo "[ERROR] COOKIE_SAME_SITE=None requires COOKIE_SECURE=true" >&2
  exit 1
fi

echo "[INFO] env validation passed: ${ENV_FILE}"
