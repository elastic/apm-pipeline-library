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
set -uxeo pipefail

# Once the npm run bundlesize goal has been run this particular
# script will create a JSON file with all the details regarding
# the size of the bundles and also the details regardinig the
# target to be compared with.
#

NAME=${1:?'Missing the name of the report'}
REPORT_FOLDER=${2:?'Missing the output folder'}
INPUT=${3:?'Missing the input files to query for'}
COMPARE_TO=${4:-''}

# Prepare temporary folder
mkdir -p "${REPORT_FOLDER}"

# Query each bundlesize html reported file
for f in ${INPUT} ; do
	DATA=$(grep "window.chartData =" "${f}" | sed 's#window.chartData =##g' | sed 's#;$##g')
    FILENAME=$(basename "${f}")
    echo "${DATA}" | jq 'map(del(.groups))' > "${REPORT_FOLDER}/${FILENAME}.json"
done

# Create a reported file
REPORT=${REPORT_FOLDER}/${NAME}.json
jq -s '[.[][]]' "${REPORT_FOLDER}"/*.json > "${REPORT}"

# Compare report with the given report
if [ -n "${COMPARE_TO}" ] ; then
    if [ -e "${COMPARE_TO}" ] ; then
        tmp="$(mktemp -d)/compare.json"
        for label in $(jq -r 'map(.label) | .[]' "${COMPARE_TO}"); do
            previousParsedSize=$(jq --arg id "${label}" '(.[] | select(.label==$id) | .parsedSize)' "${COMPARE_TO}")
            currentParsedSize=$(jq --arg id "${label}" '(.[] | select(.label==$id) | .parsedSize)' "${REPORT}")
            if [ "${previousParsedSize}" != "${currentParsedSize}" ] ; then
                jq --arg id "${label}" --argjson new "${previousParsedSize}" '(.[] | select(.label==$id) | .previousParsedSize) |= $new' "${REPORT}" > "$tmp"
                mv "$tmp" "${REPORT}"
            fi
            previousGzipSize=$(jq --arg id "${label}" '(.[] | select(.label==$id) | .gzipSize)' "${COMPARE_TO}")
            currentGzipSize=$(jq --arg id "${label}" '(.[] | select(.label==$id) | .gzipSize)' "${REPORT}")
            if [ "${previousGzipSize}" != "${currentGzipSize}" ] ; then
                jq --arg id "${label}" --argjson new "${previousGzipSize}" '(.[] | select(.label==$id) | .previousGzipSize) |= $new' "${REPORT}" > "$tmp"
                mv "$tmp" "${REPORT}"
            fi
        done
    fi
fi
