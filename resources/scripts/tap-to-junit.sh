#!/usr/bin/env bash
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
set -eo pipefail

if [ -n "${PIPELINE_LOG_LEVEL}" ] && [ "${PIPELINE_LOG_LEVEL}" == "DEBUG" ] ; then
  set -x
fi

PATTERN=${1:?'Missing the pattern'}
PACKAGENAME=${2:?'Missing the packageName'}
SUFFIX=${3:?'Missing the suffix'}
NODEVERSION=${4:?'Missing the nodeVersion'}

error=0

docker run --rm \
  -v "$(pwd)":/usr/src/app \
  -w /usr/src/app \
  -u "$(id -u)":"$(id -g)" \
  "${NODEVERSION}" \
  sh -c "export HOME=/tmp ; mkdir ~/.npm-global; npm config set prefix ~/.npm-global ; npm install tap-xunit -g ; for i in ${PATTERN} ; do (echo \${i}; cat \${i} | /tmp/.npm-global/bin/tap-xunit --package='${PACKAGENAME}' > \${i%.*}-${SUFFIX}) ; done" || error=1

exit $error
