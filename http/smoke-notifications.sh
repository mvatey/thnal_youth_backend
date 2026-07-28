#!/usr/bin/env bash
#
# smoke-notifications.sh — end-to-end smoke test for the Notification API.
#
# Exercises all four fan-out targets (ALL / BRANCH / ACTIVITY_PARTICIPANTS / USERS),
# the inbox reads, and the read-state endpoints, plus a couple of negative cases.
# Requires: bash, curl, jq.
#
# Config via env (defaults match application.properties + the seed data):
#   BASE_URL       default http://localhost:8081
#   ADMIN_LOGIN    default admin1@gmail.com
#   MEMBER_LOGIN   default member1@gmail.com
#   PASSWORD       default 12345         (seed: "Temporary password for all accounts: 12345")
#   BRANCH_ID      default 1
#   ACTIVITY_ID    default 1
#   USER_IDS       default [1,2,3]       (JSON array literal)
#
# Usage:  ./smoke-notifications.sh.sh
#         BASE_URL=http://localhost:8081 PASSWORD=12345 ./smoke-notifications.sh.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8081}"
ADMIN_LOGIN="${ADMIN_LOGIN:-admin1@gmail.com}"
MEMBER_LOGIN="${MEMBER_LOGIN:-member1@gmail.com}"
PASSWORD="${PASSWORD:-12345}"
BRANCH_ID="${BRANCH_ID:-1}"
ACTIVITY_ID="${ACTIVITY_ID:-1}"
USER_IDS="${USER_IDS:-[1,2,3]}"

command -v jq >/dev/null || { echo "jq is required (brew install jq / apt install jq)"; exit 1; }

pass=0; fail=0
green() { printf '\033[32m%s\033[0m\n' "$1"; }
red()   { printf '\033[31m%s\033[0m\n' "$1"; }
hr()    { printf '%s\n' "------------------------------------------------------------"; }

# check <label> <expected_status> <actual_status> [body]
check() {
  local label="$1" expected="$2" actual="$3" body="${4:-}"
  if [[ "$actual" == "$expected" ]]; then
    green "PASS  $label  ($actual)"; pass=$((pass+1))
  else
    red   "FAIL  $label  expected $expected, got $actual"; fail=$((fail+1))
    [[ -n "$body" ]] && echo "      body: $body"
  fi
}

# login <phoneOrEmail> -> echoes accessToken (LoginResponse is unwrapped)
login() {
  local who="$1"
  curl -s -X POST "$BASE_URL/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"phoneOrEmail\":\"$who\",\"password\":\"$PASSWORD\"}" \
  | jq -r '.accessToken'
}

# post_notif <token> <json> -> "HTTP_STATUS<newline>BODY"
post_notif() {
  local token="$1" payload="$2"
  curl -s -w '\n%{http_code}' -X POST "$BASE_URL/api/notifications" \
    -H "Authorization: Bearer $token" \
    -H 'Content-Type: application/json' \
    -d "$payload"
}

# split_status / split_body from a "body\nstatus" curl -w response
status_of() { tail -n1 <<<"$1"; }
body_of()   { sed '$d'   <<<"$1"; }

hr; echo "Notification API smoke test  ->  $BASE_URL"; hr

# ---- auth -----------------------------------------------------------------
ADMIN_TOKEN="$(login "$ADMIN_LOGIN")"
[[ "$ADMIN_TOKEN" != "null" && -n "$ADMIN_TOKEN" ]] \
  && { green "PASS  admin login"; pass=$((pass+1)); } \
  || { red "FAIL  admin login (check BASE_URL / creds / server up)"; exit 1; }

MEMBER_TOKEN="$(login "$MEMBER_LOGIN")"
[[ "$MEMBER_TOKEN" != "null" && -n "$MEMBER_TOKEN" ]] \
  && { green "PASS  member login"; pass=$((pass+1)); } \
  || { red "FAIL  member login"; fail=$((fail+1)); }

hr
# ---- create: ALL ----------------------------------------------------------
resp="$(post_notif "$ADMIN_TOKEN" '{
  "typeId":1,"title":"System maintenance","body":"Down tonight 22:00.",
  "actionUrl":"/announcements/maintenance","target":"ALL"}')"
st="$(status_of "$resp")"; bd="$(body_of "$resp")"
check "create ALL" 200 "$st" "$bd"
NOTIF_ID="$(jq -r '.data.notificationId // empty' <<<"$bd" 2>/dev/null || true)"
echo "      notificationId=$NOTIF_ID  recipientCount=$(jq -r '.data.recipientCount // "?"' <<<"$bd" 2>/dev/null || echo '?')"

# ---- create: BRANCH -------------------------------------------------------
resp="$(post_notif "$ADMIN_TOKEN" "{
  \"typeId\":1,\"title\":\"Branch meeting\",\"body\":\"Saturday.\",
  \"target\":\"BRANCH\",\"targetBranchId\":$BRANCH_ID}")"
check "create BRANCH" 200 "$(status_of "$resp")" "$(body_of "$resp")"

# ---- create: ACTIVITY_PARTICIPANTS ---------------------------------------
resp="$(post_notif "$ADMIN_TOKEN" "{
  \"typeId\":1,\"title\":\"Event reminder\",\"body\":\"Workshop tomorrow.\",
  \"activityId\":$ACTIVITY_ID,\"target\":\"ACTIVITY_PARTICIPANTS\",\"targetActivityId\":$ACTIVITY_ID}")"
check "create ACTIVITY_PARTICIPANTS" 200 "$(status_of "$resp")" "$(body_of "$resp")"

# ---- create: USERS --------------------------------------------------------
resp="$(post_notif "$ADMIN_TOKEN" "{
  \"typeId\":1,\"title\":\"Direct message\",\"body\":\"Specific users.\",
  \"target\":\"USERS\",\"targetUserIds\":$USER_IDS}")"
check "create USERS" 200 "$(status_of "$resp")" "$(body_of "$resp")"

hr
# ---- negatives ------------------------------------------------------------
resp="$(post_notif "$MEMBER_TOKEN" '{
  "typeId":1,"title":"Blocked","body":"Members cannot broadcast.","target":"ALL"}')"
check "member create -> 403" 403 "$(status_of "$resp")" "$(body_of "$resp")"

resp="$(post_notif "$ADMIN_TOKEN" '{
  "typeId":1,"title":"No branch id","body":"Rejected.","target":"BRANCH"}')"
check "BRANCH missing id -> 400" 400 "$(status_of "$resp")" "$(body_of "$resp")"

resp="$(post_notif "$ADMIN_TOKEN" '{
  "typeId":1,"title":"XSS","body":"bad url","actionUrl":"javascript:alert(1)","target":"ALL"}')"
check "unsafe actionUrl -> 400" 400 "$(status_of "$resp")" "$(body_of "$resp")"

hr
# ---- inbox reads ----------------------------------------------------------
st="$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/notifications/me" \
      -H "Authorization: Bearer $MEMBER_TOKEN")"
check "GET /me" 200 "$st"

st="$(curl -s -o /dev/null -w '%{http_code}' \
      "$BASE_URL/api/notifications/me?onlyUnread=true&page=0&size=10" \
      -H "Authorization: Bearer $MEMBER_TOKEN")"
check "GET /me (unread, paged)" 200 "$st"

unread_resp="$(curl -s "$BASE_URL/api/notifications/me/unread-count" \
      -H "Authorization: Bearer $MEMBER_TOKEN")"
check "GET /me/unread-count" 200 "$(curl -s -o /dev/null -w '%{http_code}' \
      "$BASE_URL/api/notifications/me/unread-count" -H "Authorization: Bearer $MEMBER_TOKEN")"
echo "      unread=$(jq -r '.data.unread // "?"' <<<"$unread_resp" 2>/dev/null || echo '?')"

hr
# ---- read state -----------------------------------------------------------
if [[ -n "${NOTIF_ID:-}" && "$NOTIF_ID" != "null" ]]; then
  st="$(curl -s -o /dev/null -w '%{http_code}' -X POST \
        "$BASE_URL/api/notifications/me/$NOTIF_ID/read" \
        -H "Authorization: Bearer $MEMBER_TOKEN")"
  check "POST /me/$NOTIF_ID/read" 200 "$st"
else
  red "SKIP  mark-one-read (no notificationId captured)"
fi

st="$(curl -s -o /dev/null -w '%{http_code}' -X POST \
      "$BASE_URL/api/notifications/me/read-all" \
      -H "Authorization: Bearer $MEMBER_TOKEN")"
check "POST /me/read-all" 200 "$st"

hr
echo "Results:  $pass passed, $fail failed"
hr
exit $(( fail > 0 ? 1 : 0 ))
