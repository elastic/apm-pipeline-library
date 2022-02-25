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
# It uses a version of NVM to install NodeJS, and then uses it to install
# the proper NodeJS version for building Kibana. Finally, it will generate
# the Docker image for Kibana for the current state of the Git repository.
#
set -ex

unset NVM_DIR

export BABEL_DISABLE_CACHE=true
export FORCE_COLOR=1
export NODE_OPTIONS=" --max-old-space-size=4096"
export BUILD_TS_REFS_DISABLE="true"

if [ ! -f "$HOME/.nvm/nvm.sh" ]; then
  curl -Sso- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash
  export NVM_DIR="$HOME/.nvm"
  # shellcheck disable=SC1090,SC1091
  [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
fi

if [ "v${NODE_VERSION}" != "$(node --version)" ]; then
  # shellcheck disable=SC1090,SC1091
  set +x
  . "${NVM_DIR}/nvm.sh"
  set -x
  nvm install "${NODE_VERSION}"
  nvm use --delete-prefix "v${NODE_VERSION}"
fi

BUILD_DOCKER_OPTS="--skip-initialize --skip-generic-folders --skip-platform-folders --skip-archives --docker-images --skip-docker-contexts "

if [ -z "${BUILD_DOCKER_UBI}" ]; then
  BUILD_DOCKER_OPTS="${BUILD_DOCKER_OPTS} --skip-docker-ubi"
fi
if [ -z "${BUILD_DOCKER_UBUNTU}" ]; then
  BUILD_DOCKER_OPTS="${BUILD_DOCKER_OPTS} --skip-docker-ubuntu"
fi
if [ -z "${BUILD_DOCKER_CLOUD}" ]; then
  BUILD_DOCKER_OPTS="${BUILD_DOCKER_OPTS} --skip-docker-cloud"
fi

mkdir -p ~/.npm-global/lib
npm config set prefix "${HOME}/.npm-global"
export PATH=${HOME}/.npm-global/bin:${PATH}

# if [ -d "${HOME}/.cache" ] && [ -n "${CI}" ]; then
#   ln -s "${HOME}/.cache" "$(pwd)/.cache"
# fi
# if [ -d "${HOME}/.bazel-cache" ] && [ -n "${CI}" ]; then
#   ln -s "${HOME}/.bazel-cache" "$(pwd)/.bazel-cache"
# fi

pwd
ls -la

npm install -g yarn
time yarn kbn clean
time yarn kbn bootstrap --prefer-offline --no-audit --link-duplicates
# build Linux package
time node scripts/build
# build docker images
# shellcheck disable=SC2086
time node scripts/build ${BUILD_DOCKER_OPTS}
