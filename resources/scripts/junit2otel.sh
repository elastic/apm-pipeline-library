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

set -eu

readonly DOCKER_IMAGE="mdelapenya/junit2otlp:latest"

echo "SERVICE_NAME=${SERVICE_NAME}"
echo "SERVICE_VERSION=${SERVICE_VERSION}"
echo "TEST_RESULTS_GLOB=${TEST_RESULTS_GLOB}"
echo "TRACE_NAME=${TRACE_NAME}"

for f in ${TEST_RESULTS_GLOB}
do
  echo "Sending traces for $f file..."
  # bind directly to the Docker host's network, with no network isolation
  cmd < "$f" | docker run \
    --rm -i \
    --network host \
    --env "OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT}" \
    --env "OTEL_EXPORTER_OTLP_HEADERS=${OTEL_EXPORTER_OTLP_HEADERS}" \
    ${DOCKER_IMAGE} \
    --service-name "${SERVICE_NAME}" \
    --service-version "${SERVICE_VERSION}" \
    --trace-name "${TRACE_NAME}"
done
