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

# Once the npm run bundlesize goal has been run this particular
# script will create a JSON file with all the details regarding
# the size of the bundles and also the details regardinig the
# target to be compared with.
#

if [ -n "${PIPELINE_LOG_LEVEL}" ] && [ "${PIPELINE_LOG_LEVEL}" == "DEBUG" ] ; then
  set -x
fi

NAME=${1:?'Missing the name of the report'}
REPORT_FOLDER=${2:?'Missing the output folder'}
INPUT=${3:?'Missing the input files to query for'}
COMPARE_TO=${4:-''}

# Install jq if required
JQ=$(which jq)
if [ -z "${JQ}" ]; then
  JQ="/tmp/jq"
  OS=$(uname -s)
  if [ "${OS}" == "Linux" ] ; then
    ARCH=$(uname -m)
    suffix=linux-64
    if [ "${ARCH}" != "x86_64" ] ; then
        suffix=linux-32
    fi
  else
    suffix=osx-amd64
  fi
  wget -q -O "${JQ}" https://github.com/stedolan/jq/releases/download/jq-1.6/jq-${suffix}
fi

# Prepare temporary folder
mkdir -p "${REPORT_FOLDER}"

# Query each bundlesize html reported file
for f in ${INPUT} ; do
	DATA=$(grep "window.chartData =" "${f}" | sed 's#window.chartData =##g' | sed 's#;$##g')
    FILENAME=$(basename "${f}")
    echo "${DATA}" | $JQ 'map(del(.groups))' > "${REPORT_FOLDER}/${FILENAME}.json"
done

# Create a reported file
REPORT=${REPORT_FOLDER}/${NAME}.json
$JQ -s '[.[][]]' "${REPORT_FOLDER}"/*.json > "${REPORT}"

# Compare report with the given report
if [ -n "${COMPARE_TO}" ] ; then
    if [ -e "${COMPARE_TO}" ] ; then
        tmp="$(mktemp -d)/compare.json"
        # For each entry then add the previous sizes
        for label in $($JQ -r 'map(.label) | .[]' "${COMPARE_TO}"); do
            # shellcheck disable=SC2016
            previousParsedSize=$($JQ --arg id "${label}" '(.[] | select(.label==$id) | .parsedSize)' "${COMPARE_TO}")
            # shellcheck disable=SC2016
            currentParsedSize=$($JQ --arg id "${label}" '(.[] | select(.label==$id) | .parsedSize)' "${REPORT}")
            if [ "${previousParsedSize}" != "${currentParsedSize}" ] ; then
                # shellcheck disable=SC2016
                $JQ --arg id "${label}" --argjson new "${previousParsedSize}" '(.[] | select(.label==$id) | .previousParsedSize) |= $new' "${REPORT}" > "$tmp"
                mv "$tmp" "${REPORT}"
            fi
            # shellcheck disable=SC2016
            previousGzipSize=$($JQ --arg id "${label}" '(.[] | select(.label==$id) | .gzipSize)' "${COMPARE_TO}")
            # shellcheck disable=SC2016
            currentGzipSize=$($JQ --arg id "${label}" '(.[] | select(.label==$id) | .gzipSize)' "${REPORT}")
            if [ "${previousGzipSize}" != "${currentGzipSize}" ] ; then
                # shellcheck disable=SC2016
                $JQ --arg id "${label}" --argjson new "${previousGzipSize}" '(.[] | select(.label==$id) | .previousGzipSize) |= $new' "${REPORT}" > "$tmp"
                mv "$tmp" "${REPORT}"
            fi
        done
    fi
fi
