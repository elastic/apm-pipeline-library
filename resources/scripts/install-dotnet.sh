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
# This script downloads the .NET SDKs from a GCS bucket and uncompress the content.
#
# ussage: install-dotnet.sh [DOTNET_ROOT|${HOME}/.dotnet]
#
set -eu

BUCKET="https://storage.googleapis.com/obs-ci-cache/tools"
DOTNET_ROOT=${1:-"${HOME}/.dotnet"}

mkdir -p "${DOTNET_ROOT}"
for v in '2.1.505' '3.0.103' '3.1.100' '5.0.203'
do
  FILE_NAME="dotnet-sdk-${v}-linux-x64.tar.gz"
  echo "Downloading ${BUCKET}/${FILE_NAME}"
  curl -sfSLO "${BUCKET}/${FILE_NAME}"
  tar -xzf "${FILE_NAME}" -C "${DOTNET_ROOT}"
  rm "${FILE_NAME}"
done
