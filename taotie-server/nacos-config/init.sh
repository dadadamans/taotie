#!/bin/sh
set -e

apk add --no-cache curl

NACOS_URL="http://nacos:8848"
CONFIG_DIR="/nacos-config"

echo "Waiting for Nacos to be ready..."
until curl -sf "$NACOS_URL/nacos/v1/ns/operator/servers" > /dev/null 2>&1; do
  sleep 3
done
echo "Nacos is ready."

for f in "$CONFIG_DIR"/*.yml; do
  dataId=$(basename "$f")
  echo "Publishing: $dataId"
  until curl -sf -X POST "$NACOS_URL/nacos/v1/cs/configs" \
    -d "dataId=$dataId" \
    -d "group=DEFAULT_GROUP" \
    --data-urlencode "content@$f"; do
    echo "Retrying $dataId..."
    sleep 3
  done
  echo "Published: $dataId"
done

echo "All Nacos configs initialized successfully."
