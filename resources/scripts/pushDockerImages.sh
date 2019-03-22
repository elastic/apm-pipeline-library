#!#!/usr/bin/env bash
set -euo pipefail

#USER_PATH="employees/user"
USER_VERSION="daily"
REGISTRY="docker.elastic.co"
VERSION="7.0.0-rc1"

for image in elasticsearch/elasticsearch kibana/kibana apm/apm-server beats/auditbeat beats/filebeat beats/heartbeat beats/metricbeat beats/packetbeat
do
  IMAGE_SHORT=${image#*/}
  docker tag "${REGISTRY}/${image}:${VERSION}" "${REGISTRY}/${USER_PATH}/${IMAGE_SHORT}:${USER_VERSION}"
  docker push "${REGISTRY}/${USER_PATH}/${IMAGE_SHORT}:${USER_VERSION}"
done
