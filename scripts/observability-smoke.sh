#!/usr/bin/env bash
set -euo pipefail

: "${GRAFANA_ADMIN_PASSWORD:?Set GRAFANA_ADMIN_PASSWORD to run the local observability smoke.}"

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root_dir"
network="${APP_NETWORK:-dondok-network}"
app_url="http://localhost:${APP_PORT:-8080}"
prometheus_url="http://localhost:${PROMETHEUS_PORT:-9090}"
grafana_url="http://localhost:${GRAFANA_PORT:-3000}"

# The monitoring Compose file intentionally consumes this pre-existing app network.
docker network inspect "$network" >/dev/null 2>&1 || docker network create "$network" >/dev/null

docker compose -f compose.yaml -f compose.observability.yaml --profile observability config -q
GRAFANA_ADMIN_PASSWORD="$GRAFANA_ADMIN_PASSWORD" docker compose -f monitoring/compose.yaml config -q

docker compose -f compose.yaml -f compose.observability.yaml --profile observability up -d --build
GRAFANA_ADMIN_PASSWORD="$GRAFANA_ADMIN_PASSWORD" docker compose -f monitoring/compose.yaml up -d

prometheus_reloaded=false
for _ in $(seq 1 30); do
  if curl --fail --silent "$prometheus_url/-/ready" >/dev/null \
    && curl --fail --silent --request POST "$prometheus_url/-/reload" >/dev/null; then
    prometheus_reloaded=true
    break
  fi
  sleep 2
done
if [ "$prometheus_reloaded" != true ]; then
  echo "Prometheus did not become ready to reload its bind-mounted configuration." >&2
  exit 1
fi

grafana_status=""
for _ in $(seq 1 30); do
  grafana_status="$(curl --silent --output /dev/null --write-out '%{http_code}' -u "${GRAFANA_ADMIN_USER:-admin}:$GRAFANA_ADMIN_PASSWORD" "$grafana_url/api/user" || true)"
  if [ "$grafana_status" = "200" ]; then
    break
  fi
  if [ "$grafana_status" = "401" ]; then
    cat >&2 <<'EOF'
Grafana rejected GRAFANA_ADMIN_PASSWORD. Its admin password is persisted in the local grafana-data volume and is only initialized on first start. Re-run with that existing local password; this script will not reset or delete volumes.
EOF
    exit 1
  fi
  sleep 2
done
if [ "$grafana_status" != "200" ]; then
  echo "Grafana did not become ready for authenticated checks (last HTTP status: $grafana_status)." >&2
  exit 1
fi

for _ in $(seq 1 60); do
  if curl --fail --silent "$app_url/api/actuator/health/readiness" | grep -q '"status":"UP"'; then
    break
  fi
  sleep 2
done
curl --fail --silent "$app_url/api/actuator/health/readiness" | grep -q '"status":"UP"'

for _ in $(seq 1 30); do
  if curl --fail --silent "$prometheus_url/api/v1/targets" | python3 -c '
import json, sys
for target in json.load(sys.stdin)["data"]["activeTargets"]:
    if target["labels"].get("job") == "dondok-api-local" and target["health"] == "up":
        raise SystemExit(0)
raise SystemExit(1)
'; then
    break
  fi
  sleep 2
done
curl --fail --silent "$prometheus_url/api/v1/targets" | python3 -c '
import json, sys
assert any(t["labels"].get("job") == "dondok-api-local" and t["health"] == "up" for t in json.load(sys.stdin)["data"]["activeTargets"])
'

# Protected point-history and settlement-detail reads create generic HTTP meters; no domain data is mutated.
curl --silent --output /dev/null "$app_url/api/points/history"
curl --silent --output /dev/null "$app_url/api/settlements/1"
assert_prometheus_baseline_metrics() {
  python3 - "$prometheus_url" <<'PYTHON'
import json
import sys
from urllib.parse import urlencode
from urllib.request import urlopen

base_url = sys.argv[1]
queries = {
    "HTTP": 'http_server_requests_seconds_count{job="dondok-api-local"}',
    "process CPU": 'process_cpu_usage{job="dondok-api-local"}',
    "JVM total memory": 'sum(jvm_memory_used_bytes{job="dondok-api-local"})',
    "JVM heap": 'jvm_memory_used_bytes{job="dondok-api-local",area="heap"}',
    "JVM GC": 'jvm_gc_pause_seconds_count{job="dondok-api-local"}',
    "Hikari": 'hikaricp_connections_active{job="dondok-api-local"}',
}
for name, query in queries.items():
    with urlopen(f"{base_url}/api/v1/query?{urlencode({'query': query})}") as response:
        payload = json.load(response)
    assert payload["status"] == "success", f"Prometheus query failed for {name}"
    assert payload["data"]["result"], f"Prometheus has no local {name} metric data after smoke traffic"
PYTHON
}

for _ in $(seq 1 30); do
  if assert_prometheus_baseline_metrics 2>/dev/null; then
    break
  fi
  sleep 2
done
assert_prometheus_baseline_metrics
curl --fail --silent -u "${GRAFANA_ADMIN_USER:-admin}:$GRAFANA_ADMIN_PASSWORD" "$grafana_url/api/dashboards/uid/point-settlement-baseline" | grep -q 'Point & Settlement Baseline'

echo "Observability smoke passed: readiness UP, Prometheus local target UP, generic HTTP metrics scraped, dashboard provisioned."
