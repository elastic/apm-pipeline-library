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

unset NVM_DIR

export BABEL_DISABLE_CACHE=true
export FORCE_COLOR=1
export NODE_OPTIONS=" --max-old-space-size=4096"
export BUILD_TS_REFS_DISABLE="true"

if [ -z "$(command -v nvm)" ]; then
  curl -Sso- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.3/install.sh | bash
  export NVM_DIR="$HOME/.nvm"
  # shellcheck disable=SC1090,SC1091
  [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
fi

if [ "v${NODE_VERSION}" != "$(node --version)" ]; then
  # shellcheck disable=SC1090,SC1091
  . "${NVM_DIR}/nvm.sh"
  nvm install "${NODE_VERSION}"
  nvm use "${NODE_VERSION}"
fi

npm install -g yarn
yarn config set cache-folder "${HOME}/.yarn_cache"
yarn kbn clean
yarn kbn bootstrap
# build Linux package
node scripts/build
# build docker-ubuntu and docker-cloud
node scripts/build \
  --skip-initialize \
  --skip-generic-folders \
  --skip-platform-folders \
  --skip-archives \
  --docker-images \
  --skip-docker-ubi \
  --skip-docker-contexts
