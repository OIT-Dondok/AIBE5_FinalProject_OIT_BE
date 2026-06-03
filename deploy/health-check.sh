#!/usr/bin/env bash
set -euo pipefail

URL="${1:?usage: health-check.sh <url> [max_attempts] [interval_seconds] [timeout_seconds]}"
MAX_ATTEMPTS="${2:-24}"
INTERVAL_SECONDS="${3:-5}"
TIMEOUT_SECONDS="${4:-3}"

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
  status_code="$(
    curl \
      --silent \
      --show-error \
      --output /dev/null \
      --write-out '%{http_code}' \
      --max-time "${TIMEOUT_SECONDS}" \
      "${URL}" || true
  )"

  if [ "${status_code}" = "200" ]; then
    echo "[INFO] health check passed: ${URL}"
    exit 0
  fi

  echo "[INFO] health check waiting (${attempt}/${MAX_ATTEMPTS}): ${URL} returned ${status_code}"
  sleep "${INTERVAL_SECONDS}"
done

echo "[ERROR] health check failed after ${MAX_ATTEMPTS} attempts: ${URL}" >&2
exit 1
