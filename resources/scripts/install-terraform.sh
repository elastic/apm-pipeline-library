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
#
# This script downloads terraform from a GCS bucket and uncompress the content.
#
# ussage: install-dotnet.sh [TOOL_ROOT|${HOME}/bin] [TERRAFORM_VERSION|0.15.3]
#
set -eu

BUCKET="https://storage.googleapis.com/obs-ci-cache/tools"
TOOL_ROOT=${1:-"${HOME}/bin"}
TERRAFORM_VERSION=${2:-"0.15.3"}
FILE_NAME="terraform_${TERRAFORM_VERSION}_linux_amd64.zip"

mkdir -p "${TOOL_ROOT}"
echo "Downloading ${BUCKET}/${FILE_NAME}"
curl -sfSLO "${BUCKET}/${FILE_NAME}"
unzip "${FILE_NAME}" -d "${TOOL_ROOT}"
rm "${FILE_NAME}"
