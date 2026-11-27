#!/usr/bin/env bash
# Prepare one QA case in gr4vy-kotlin-client-app on the running emulator.
#
# Creates a fresh sandbox checkout session, clears logcat and launches the app with `qa_*`
# extras that the client app writes into its settings. Then open Tokenize and tap PUT, or
# use scripts/qa/adrive.py. Read the SDK log with: adb logcat -d -s Gr4vySDK
#
# Usage: [HOST=component|appcompat] scripts/qa/android-seed.sh <currency> <card_number> <cvv> [preferred_scheme] [authenticate]
#
# HOST picks the activity the app runs in, which decides how the 3DS SDK shows its screens
# (see "Host activity" in docs/QA.md): component (default) is MainActivity, a ComponentActivity;
# appcompat is AppCompatHostActivity, an AppCompatActivity.
#
# Needs the gr4vy CLI with an active sandbox profile, and a client app build that reads the
# qa_* launch extras (see docs/QA.md). Never prints the token.
set -euo pipefail

ADB=${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}
PKG=com.gr4vy.gr4vy_kotlin_client_app
HOST=${HOST:-component}
case "$HOST" in
  component) ACTIVITY=.MainActivity ;;
  appcompat) ACTIVITY=.AppCompatHostActivity ;;
  *) echo "HOST must be component or appcompat" >&2; exit 1 ;;
esac
CURRENCY=$1; PAN=$2; CVV=$3; PREFERRED=${4:-}; AUTHENTICATE=${5:-true}
ID=$(gr4vy config show | awk '$1=="id"{print $2}')
MERCHANT=$(gr4vy config show | awk '$1=="merchant_account_id"{print $2}')
[ "$(gr4vy config show | awk '$1=="environment"{print $2}')" = "sandbox" ] || { echo "Sandbox profiles only" >&2; exit 1; }

SESSION=$(gr4vy checkout-sessions create --compact \
  --data "{\"amount\":1299,\"currency\":\"$CURRENCY\",\"country\":\"GB\"}" |
  python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

"$ADB" shell am force-stop "$PKG"
"$ADB" logcat -c
"$ADB" shell am start -n "$PKG/$ACTIVITY" \
  --es qa_session "$SESSION" --es qa_gr4vy_id "$ID" --es qa_merchant_id "${MERCHANT:-default}" \
  --es qa_token "$(gr4vy token -e 30m)" --es qa_pan "$PAN" --es qa_cvv "$CVV" \
  --es qa_authenticate "$AUTHENTICATE" --es qa_preferred_scheme "'$PREFERRED'" > /dev/null
echo "session=$SESSION host=$HOST currency=$CURRENCY card=${PAN:0:6}… preferred='$PREFERRED' authenticate=$AUTHENTICATE"
