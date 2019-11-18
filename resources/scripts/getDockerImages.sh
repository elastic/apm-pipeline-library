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
MANIFEST=metadata.txt
curl -sSf "https://artifacts-api.elastic.co/v1/versions/${VERSION_ID}/builds/latest/" | jq 'del(.manifests)' > "${MANIFEST}"
BUILD_VERSION=$(jq -r ".build.version"<"${MANIFEST}")

for product in elasticsearch kibana apm-server
do
  URL=$(jq -r ".build.projects.\"${product}\".packages[]|select(.type==\"docker\" and (.classifier==\"docker-image\" or .classifier==null) ).url" <"${MANIFEST}"|grep "${product}-${BUILD_VERSION}")
  echo "Downloading ${product} - ${URL}"
  curl "${URL}"|docker load
done

for product in auditbeat filebeat heartbeat metricbeat packetbeat
do
  URL=$(jq -r ".build.projects.beats.packages[]|select(.type==\"docker\" and (.classifier==\"docker-image\" or .classifier==null) ).url" <"${MANIFEST}"|grep "${product}-${BUILD_VERSION}")
  echo "Downloading ${product} - ${URL}"
  curl "${URL}"|docker load
done
