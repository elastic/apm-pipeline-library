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

set +x

echo "dummy script"

docker --version || true
docker-compose --version || true
java -version || true
go version || true
gvm --version || true
git version || true
mvn --version || true
node --version || true
npm --version || true
python --version || true
python3 --version || true
vault --version || true
jq --version || true

JAVA_HOME="${HUDSON_HOME}/.java/java10"
PATH="${JAVA_HOME}/bin:${PATH}"
java -version || true

ls -la "${HUDSON_HOME}/.java/" || true

uname -a || true
df -h || true

docker images || true

eval "${PARAM_WITH_DEFAULT_VALUE}" || true
