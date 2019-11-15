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

## Prepare the context
if [ -e images.sha256.txt ] ; then
  rm images.sha256.txt
fi

VERSION=$(curl -s "https://artifacts-api.elastic.co/v1/versions/${VERSION}/builds/latest"|jq -r .build.version)

for image in elasticsearch/elasticsearch kibana/kibana apm/apm-server beats/auditbeat beats/filebeat beats/heartbeat beats/metricbeat beats/packetbeat
do
  echo "Let's cache the docker image for ${image}"
  IMAGE_SHORT=${image#*/}
  IMAGE_ALREADY_CACHE="${REGISTRY}/${image}:${VERSION}"
  IMAGE_TO_BE_PUSHED_IF_CHANGED="${REGISTRY}/${PREFIX}/${IMAGE_SHORT}:${PUSH_VERSION}"

  ## Get sha256
  SHA_IMAGE_ALREADY_CACHE=$(docker inspect --format='{{index .RepoDigests 0}}' "${IMAGE_ALREADY_CACHE}")
  docker pull "${IMAGE_TO_BE_PUSHED_IF_CHANGED}" > /dev/null
  SHA_IMAGE_TO_BE_PUSHED_IF_CHANGED=$(docker inspect --format='{{index .RepoDigests 0}}' "${IMAGE_TO_BE_PUSHED_IF_CHANGED}")
  docker rmi -f "${IMAGE_TO_BE_PUSHED_IF_CHANGED}" > /dev/null

  ## It has not been cached then
  if [ "$SHA_IMAGE_ALREADY_CACHE" != "$SHA_IMAGE_TO_BE_PUSHED_IF_CHANGED" ]; then
    docker tag "${IMAGE_ALREADY_CACHE}" "${IMAGE_TO_BE_PUSHED_IF_CHANGED}"
    docker push "${IMAGE_TO_BE_PUSHED_IF_CHANGED}"
  else
    echo "Image '${image}' was already pushed"
  fi

  ## Create file
  echo "${IMAGE_TO_BE_PUSHED_IF_CHANGED}:${SHA_IMAGE_TO_BE_PUSHED_IF_CHANGED}" >> images.sha256.txt
done
