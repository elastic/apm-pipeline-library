#!#!/usr/bin/env bash
set -euo pipefail

#VERSION=7.0
JSON=$(curl https://staging.elastic.co/latest/${VERSION}.json)
BUILD_ID=$(echo ${JSON}|jq -r .build_id)
VERSION_ID=$(echo ${JSON}|jq -r .version)

for product in elasticsearch kibana apm-server auditbeat filebeat heartbeat metricbeat packetbeat
do
  URL="https://staging.elastic.co/${BUILD_ID}/docker/${product}-${VERSION_ID}.tar.gz"
  echo "Downloading ${product} - ${URL}"
 curl ${URL}|docker load
done
