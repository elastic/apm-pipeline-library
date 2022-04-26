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

# Once the coverage has done this particular
# script will create a JSON file with all the details regarding
# the coverage and also the details regarding the
# target to be compared with.
#

if [ -n "${PIPELINE_LOG_LEVEL}" ] && [ "${PIPELINE_LOG_LEVEL}" == "DEBUG" ] ; then
  set -x
fi

NAME=${1:?'Missing the name of the report'}
REPORT_FOLDER=${2:?'Missing the output folder'}
# shellcheck disable=SC2034
INPUT=${3:?'Missing the input files to query for'}
COMPARE_TO=${4:-''}

# Function to add the previous metrics for the given coverage id
# if there is something to compare with.
function addPreviousValueIfPossible() {
    label=$1
    value=$2
    new_value=$3
    report=$4
    compare_to=$5

    if [ ! -e "${compare_to}" ] ; then
        return
    fi
    # shellcheck disable=SC2016
    previous_value=$($JQ -r --arg id "$label" --arg v "$value" '.[$id] | .[$v]' "${compare_to}")
    if [ -n "${previous_value}" ] ; then
        # Append the new value to the given key hash
        sed -ibck "s#\(\"$label\".*\)#\1 \n    \"$new_value\": $previous_value, #g" "${report}"
    fi
}

# Install jq if required
JQ=$(command -v jq || true)
if [ -z "${JQ}" ]; then
    echo "1..5 install jq"
    JQ="/tmp/jq"
    OS=$(uname -s)
    if [ "${OS}" == "Linux" ] ; then
        ARCH=$(uname -m)
        suffix=linux64
        if [ "${ARCH}" != "x86_64" ] ; then
            suffix=linux32
        fi
    else
        suffix=osx-amd64
    fi
    wget -q --retry-connrefused -O "${JQ}" https://github.com/stedolan/jq/releases/download/jq-1.6/jq-${suffix}
    chmod 755 "${JQ}"
else
    echo "1..3 install jq is not required"
fi

# Prepare temporary folder
echo "2..3 prepare a temporary folder"
mkdir -p "${REPORT_FOLDER}"

REPORT=${REPORT_FOLDER}/${NAME}.json

# It uses the API cobertura/api/json, therefore there is only one file
cp "${INPUT}" "${REPORT}"

if [ -n "${COMPARE_TO}" ] ; then
    if [ -e "${COMPARE_TO}" ] ; then
        echo "3..3 compare is enabled"
        # For each entry then add the previous sizes
        for key in $($JQ -r 'keys | .[]' "${COMPARE_TO}"); do
            addPreviousValueIfPossible "${key}" "ratio" "previousRatio" "${REPORT}" "${COMPARE_TO}"
            addPreviousValueIfPossible "${key}" "numerator" "previousNumerator" "${REPORT}" "${COMPARE_TO}"
            addPreviousValueIfPossible "${key}" "denominator" "previousDenominator" "${REPORT}" "${COMPARE_TO}"
        done
    else
        echo "3..3 compare is disabled since the provided file does not exist."
    fi
else
    echo "3..3 compare is disabled since no file to compare with has been provided"
fi

echo "Report has been generated. See ${REPORT}"
