#!#!/usr/bin/env bash
set -euo pipefail

#PREFIX="employees/user"
#PUSH_VERSION="daily"
#REGISTRY="docker.elastic.co"
#VERSION="7.0.0-rc1"

if [ $# -lt 2 ]; then
  echo "${0} VERSION PREFIX [PUSH_VERSION|'daily'] [REGISTRY|'docker.elastic.co']"
  exit 1
fi

VERSION=${1:-}
PREFIX=${2:-}
PUSH_VERSION=${3:-"daily"}
REGISTRY=${4:-"docker.elastic.co"}

for image in elasticsearch/elasticsearch kibana/kibana apm/apm-server beats/auditbeat beats/filebeat beats/heartbeat beats/metricbeat beats/packetbeat
do
  IMAGE_SHORT=${image#*/}
  docker tag "${REGISTRY}/${image}:${VERSION}" "${REGISTRY}/${PREFIX}/${IMAGE_SHORT}:${PUSH_VERSION}"
  docker push "${REGISTRY}/${PREFIX}/${IMAGE_SHORT}:${PUSH_VERSION}"
done
