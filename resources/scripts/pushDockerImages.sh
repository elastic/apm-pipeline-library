#!/usr/bin/env bash
# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
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

VERSION=$(curl -s "https://artifacts-api.elastic.co/v1/versions/${VERSION}/builds/latest"|jq -r .build.version)

for image in elasticsearch/elasticsearch kibana/kibana apm/apm-server beats/auditbeat beats/filebeat beats/heartbeat beats/metricbeat beats/packetbeat
do
  IMAGE_SHORT=${image#*/}
  docker tag "${REGISTRY}/${image}:${VERSION}" "${REGISTRY}/${PREFIX}/${IMAGE_SHORT}:${PUSH_VERSION}"
  docker push "${REGISTRY}/${PREFIX}/${IMAGE_SHORT}:${PUSH_VERSION}"
done
