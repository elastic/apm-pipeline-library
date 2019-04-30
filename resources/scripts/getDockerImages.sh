#!#!/usr/bin/env bash
set -exuo pipefail

if [ $# -lt 1 ]; then
  echo "${0} VERSION"
  exit 1
fi

VERSION_ID=${1}

#VERSION=7.0
#https://artifacts-api.elastic.co/v1/versions/${VERSION}/builds/latest
#JSON=$(curl https://staging.elastic.co/latest/${VERSION}.json)
#BUILD_ID=$(echo ${JSON}|jq -r .build_id)
#VERSION_ID=$(echo ${JSON}|jq -r .version)
#MANIFEST_URL=$(echo ${JSON}|jq -r .manifest_url)
MANIFEST_JSON=$(curl -sSf https://artifacts-api.elastic.co/v1/versions/${VERSION_ID}/builds/latest/)
#VERSION_ID=$(echo ${MANIFEST_JSON}|jq -r .version)

for product in elasticsearch kibana apm-server
do
  URL=$(echo ${MANIFEST_JSON}|jq -r ".build.projects.\"${product}\".packages[]|select(.type==\"docker\" and (.classifier==\"docker-image\" or .classifier==null) ).url"|grep "${product}-${VERSION_ID}")
  echo "Downloading ${product} - ${URL}"
  curl ${URL}|docker load
done

for product in auditbeat filebeat heartbeat metricbeat packetbeat
do
  URL=$(echo ${MANIFEST_JSON}|jq -r ".build.projects.beats.packages[]|select(.type==\"docker\" and (.classifier==\"docker-image\" or .classifier==null) ).url"|grep "${product}-${VERSION_ID}")
  echo "Downloading ${product} - ${URL}"
  curl ${URL}|docker load
done
