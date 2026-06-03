#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${1:-/opt/dondok/.env}"

has_env_value() {
  key="$1"
  [ -f "${ENV_FILE}" ] || return 1

  awk -v key="${key}" '
    /^[[:space:]]*($|#)/ { next }
    {
      line = $0
      sub(/\r$/, "", line)
      split(line, parts, "=")
      env_key = parts[1]
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", env_key)

      if (env_key == key) {
        sub(/^[^=]*=/, "", line)
        if (length(line) > 0) {
          found = 1
        }
        exit
      }
    }
    END {
      if (!found) {
        exit 1
      }
    }
  ' "${ENV_FILE}"
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "[ERROR] required command is not installed: $1" >&2
    exit 1
  }
}

require_command docker
require_command nginx
require_command curl

docker info >/dev/null 2>&1 || {
  echo "[ERROR] current EC2 user cannot access Docker. Add the user to the docker group and reconnect." >&2
  exit 1
}

metadata_token="$(
  curl \
    --silent \
    --max-time 2 \
    -X PUT \
    -H "X-aws-ec2-metadata-token-ttl-seconds: 60" \
    "http://169.254.169.254/latest/api/token" || true
)"

iam_role=""
if [ -n "${metadata_token}" ]; then
  iam_role="$(
    curl \
      --silent \
      --max-time 2 \
      -H "X-aws-ec2-metadata-token: ${metadata_token}" \
      "http://169.254.169.254/latest/meta-data/iam/security-credentials/" || true
  )"
fi

if [ -z "${iam_role}" ]; then
  if has_env_value AWS_ACCESS_KEY_ID && has_env_value AWS_SECRET_ACCESS_KEY; then
    echo "[WARN] EC2 IAM role was not detected. Falling back to AWS access keys from env."
  else
    echo "[ERROR] AWS credentials were not found. Attach an EC2 IAM role for S3 access or set AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY." >&2
    exit 1
  fi
else
  echo "[INFO] EC2 IAM role detected for AWS access: ${iam_role}"
fi

echo "[INFO] runtime validation passed"
