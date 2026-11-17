#!/usr/bin/env bash
# Probe the 3DS versioning endpoint directly, before testing the SDK.
#
# Creates one checkout session per query variant, stores the card on it, then calls
# GET /checkout/sessions/{id}/three-d-secure-version with that query and prints the status.
# This tells you what the server will do, so the SDK runs have a known expected result.
#
# Usage: scripts/qa/probe-versioning.sh <card_number> <currency> <cvv> [query ...]
#   scripts/qa/probe-versioning.sh 4556557955726624 GBP 123 "" "?preferred_scheme=visa" "?preferredScheme=visa"
#
# Needs the gr4vy CLI with an active sandbox profile (`gr4vy config list`). Never prints the token
# or the `api_key` the versioning response carries for the 3DS SDK.
set -euo pipefail

CARD=$1; CURRENCY=$2; CVV=$3; shift 3
QUERIES=("$@"); [ ${#QUERIES[@]} -eq 0 ] && QUERIES=("")

ID=$(gr4vy config show | awk '$1=="id"{print $2}')
ENV=$(gr4vy config show | awk '$1=="environment"{print $2}')
[ "$ENV" = "sandbox" ] || { echo "Refusing to run against $ENV" >&2; exit 1; }
BASE="https://api.sandbox.$ID.gr4vy.app"
TOKEN=$(gr4vy token -e 30m)

for q in "${QUERIES[@]}"; do
  sid=$(gr4vy checkout-sessions create --compact \
    --data "{\"amount\":1299,\"currency\":\"$CURRENCY\",\"country\":\"GB\"}" |
    python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
  curl -sf -o /dev/null -X PUT "$BASE/checkout/sessions/$sid/fields" \
    -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
    -d "{\"payment_method\":{\"method\":\"card\",\"number\":\"$CARD\",\"expiration_date\":\"01/30\",\"security_code\":\"$CVV\"}}"
  out=$(curl -s -w '\n%{http_code}' "$BASE/checkout/sessions/$sid/three-d-secure-version$q" \
    -H "Authorization: Bearer $TOKEN")
  printf '%-40s -> %s %s\n' "${q:-(no query)}" "${out##*$'\n'}" "$(printf '%s' "${out%$'\n'*}" | sed -E 's/"api_key" *: *"[^"]*"/"api_key":"[redacted]"/' | head -c 120)"
done
