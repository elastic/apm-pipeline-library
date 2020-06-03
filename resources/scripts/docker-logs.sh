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

#
# For the Jenkins step and optional docker compose file
# then query all the docker containers and export their
# docker logs as individual files.
#

set -euo pipefail

STEP=${1:-""}
DOCKER_COMPOSE=${2:-"docker-compose.yml"}

DOCKER_INFO_DIR="docker-info/${STEP}"
mkdir -p "${DOCKER_INFO_DIR}"

if [ -e "${DOCKER_COMPOSE}" ] ; then
  cp "${DOCKER_COMPOSE}" "${DOCKER_INFO_DIR}"
fi
cd "${DOCKER_INFO_DIR}"

docker ps -a &> docker-containers.txt

DOCKER_IDS=$(docker ps -aq)

for id in ${DOCKER_IDS}
do
  docker ps -af id="${id}" --no-trunc &> "${id}-cmd.txt"
  docker logs "${id}" &> "${id}.log" || echo "It is not possible to grab the logs of ${id}"
  (docker inspect "${id}" &> "${id}-inspect.json" | jq 'del(.[].Config.Env)' || echo "It is not possible to grab the inspect of ${id}"
done
