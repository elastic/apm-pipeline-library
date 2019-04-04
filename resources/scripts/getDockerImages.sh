#!#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "${0} VERSION"
  exit 1
fi

VERSION=${1}

#VERSION=7.0
#https://artifacts-api.elastic.co/v1/versions/${VERSION}/builds/latest
JSON=$(curl https://staging.elastic.co/latest/${VERSION}.json)
BUILD_ID=$(echo ${JSON}|jq -r .build_id)
VERSION_ID=$(echo ${JSON}|jq -r .version)
MANIFEST_URL=$(echo ${JSON}|jq -r .manifest_url)
MANIFEST_JSON=$(curl ${MANIFEST_URL})

for product in elasticsearch kibana apm-server
do
  URL=$(echo ${MANIFEST_JSON}|jq -r ".projects.\"${product}\".packages[]|select(.type==\"docker\").url"|grep "${product}-${VERSION_ID}")
  echo "Downloading ${product} - ${URL}"
  curl ${URL}|docker load
done

for product in auditbeat filebeat heartbeat metricbeat packetbeat
do
  URL=$(echo ${MANIFEST_JSON}|jq -r ".projects.beats.packages[]|select(.type==\"docker\").url"|grep "${product}-${VERSION_ID}")
  echo "Downloading ${product} - ${URL}"
  curl ${URL}|docker load
done
