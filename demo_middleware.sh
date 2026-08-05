#!/bin/bash

API="http://localhost:8080/api"

req() {
    local method=$1; local url=$2; local token=$3; local idem=$4; local data=$5
    local args=(-s -w "\n%{http_code}" -X "$method" "$API$url")
    [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
    [ -n "$idem"  ] && args+=(-H "Idempotency-Key: $idem")
    [ -n "$data"  ] && args+=(-H "Content-Type: application/json" -d "$data")
    curl "${args[@]}"
}

split_response() {
    body=$(echo "$1" | head -n -1)
    status=$(echo "$1" | tail -n 1)
}

echo "============================================="
echo "resetting db and redis..."
docker exec -i taxol760-redis redis-cli FLUSHALL > /dev/null
docker exec -i taxol760-db psql -U taxol760 -d taxol760 \
    -c "TRUNCATE TABLE rides, vehicles, drivers, users RESTART IDENTITY CASCADE;" > /dev/null
echo "done."
echo ""

raw=$(req POST /auth/register "" "" '{"email":"mw-rider@example.com","name":"MW Rider","password":"pass","phoneNumber":"1001"}')
split_response "$raw"; USER_TOKEN=$(echo "$body" | jq -r '.token')

raw=$(req POST /auth/register "" "" '{"email":"mw-driver@example.com","name":"MW Driver","password":"pass","phoneNumber":"1002"}')
split_response "$raw"; DRIVER_TOKEN=$(echo "$body" | jq -r '.token')

req POST /drivers/me "$DRIVER_TOKEN" "" \
    '{"licenseNumber":"MW-001","vehicleBrand":"T","vehicleModel":"T","vehicleColor":"T","vehiclePlateNumber":"T"}' > /dev/null

raw=$(req GET /drivers/me "$DRIVER_TOKEN"); split_response "$raw"
DRIVER_ID=$(echo "$body" | jq -r '.id')

RIDE_BODY="{\"driverId\":$DRIVER_ID,\"pickupLatitude\":10,\"pickupLongitude\":10,\"dropoffLatitude\":20,\"dropoffLongitude\":20}"

# ─────────────────────────────────────────
echo "============================================="
echo "idempotency key protection"
echo "============================================="
echo ""

IDEM_KEY="ride-idem-demo-001"

echo "-> first request (Idempotency-Key: $IDEM_KEY)"
sleep 1
raw=$(req POST /rides "$USER_TOKEN" "$IDEM_KEY" "$RIDE_BODY")
split_response "$raw"
echo "   HTTP $status"
echo "$body" | jq .
echo ""

echo "-> sending identical request again..."
raw=$(req POST /rides "$USER_TOKEN" "$IDEM_KEY" "$RIDE_BODY")
split_response "$raw"
echo "   HTTP $status — blocked:"
echo "$body" | jq .
echo ""
sleep 5

# ─────────────────────────────────────────
echo "============================================="
echo "rate limiting filter"
echo "============================================="
echo ""

echo "-> first request"
sleep 0.5
raw=$(req GET /users/me "$USER_TOKEN")
split_response "$raw"
echo "   HTTP $status"
echo "$body" | jq .
echo ""

echo "-> ramping up requests..."
for i in $(seq 2 20); do
    raw=$(req GET /users/me "$USER_TOKEN")
    split_response "$raw"

    if [ "$status" -eq 429 ]; then
        echo ""
        echo "-> request #$i blocked. HTTP $status"
        echo "$body" | jq .
        break
    fi

    printf "\r   request #%d: HTTP %s" "$i" "$status"

    # slow for first 5, then fast
    if [ "$i" -le 6 ]; then
        sleep 0.7
    fi
done

echo ""
echo "============================================="
