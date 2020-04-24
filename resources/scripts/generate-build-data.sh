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

if [ -n "${PIPELINE_LOG_LEVEL}" ] && [ "${PIPELINE_LOG_LEVEL}" == "DEBUG" ] ; then
    set -x
fi

BO_JOB_URL=${1:?'Missing the Blue Ocean Job URL'}
BO_BUILD_URL=${2:?'Missing the Blue Ocean Build URL'}
RESULT=${3:?'Missing the build result'}
DURATION=${4:?'Missing the build duration'}

## To report the status afterwards
STATUS=0
BUILD_INFO="build-info.json"
BUILD_REPORT="build-report.json"
UTILS_LIB='/usr/local/bin/bash_standard_lib.sh'

DEFAULT_HASH="{ }"
DEFAULT_LIST="[ ]"

if [ -e "${UTILS_LIB}" ] ; then
    # shellcheck disable=SC1091,SC1090
    source "${UTILS_LIB}"
fi

### Functions
function sedCommand() {
    flag="-i"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        flag="-i ''"
    fi
    sed "${flag}" "$@"
}

function prettyJson() {
    tmp=$(mktemp)
    if [ -x "$(command -v jq)" ] ; then
        jq '.' "${1}" > "${tmp}" && mv "${tmp}" "{1}"
    elif [ -x "$(command -v python3)" ] ; then
        python3 -m json.tool < "${1}" > "${tmp}" && mv "${tmp}" "${1}"
    else
        echo 'INFO: pretty cannot be executed'
    fi
}

function curlCommand() {
    curl --silent --max-time 60 --connect-timeout 30 -o "$1" "$2"
}

function fetch() {
    file=$1
    url=$2

    echo "INFO: curl ${url} -o ${file}"
    ## Let's support retry in the CI.
    if [[ -n "${JENKINS_URL}" && -e "${UTILS_LIB}" ]] ; then
        (retry 3 curlCommand "${file}" "${url}") || STATUS=1
    else
        curlCommand "${file}" "${url}" || STATUS=1
    fi
}

function fetchAndDefault() {
    file=$1
    url=$2
    default=$3

    fetch "$file" "$url"

    if [ ! -e "${file}" ] ; then
        echo "${default}" > "${file}"
    fi
}

function fetchAndPrepareBuildInfo() {
    file=$1
    url=$2
    key=$3
    default=$4

    fetchAndDefault "${file}" "${url}" "${default}"

    ### Manipulate build result and time
    if [ -x "$(command -v jq)" ] ; then
        tmp=$(mktemp)
        jq --arg a "${RESULT}" '.result = $a' "${file}" > "$tmp" && mv "$tmp" "${file}"
        jq --arg a "${DURATION}" '.durationInMillis = $a' "${file}" > "$tmp" && mv "$tmp" "${file}"
        jq '.state = "FINISHED"' "${file}" > "$tmp" && mv "$tmp" "${file}"
    else
        sedCommand "s#\"durationInMillis\":[0-9]*,#\"durationInMillis\":${DURATION},#g" "${file}"
        sedCommand "s#\"result\":\"[a-zA-Z]*\"#\"result\":\"${RESULT}\"#g" "${file}"
        sedCommand "s#\"state\":\"[a-zA-Z]*\"#\"state\":\"FINISHED\"#g" "${file}"
    fi

    echo "\"${key}\": $(cat "${file}")" >> "${BUILD_REPORT}"
}

function fetchAndPrepareBuildReport() {
    fetchAndPrepare "$1" "$2" "$3" "$4" "${BUILD_REPORT}"
}

function fetchAndPrepare() {
    file=$1
    url=$2
    key=$3
    default=$4
    output=$5

    fetchAndDefault "${file}" "${url}" "${default}"

    echo "\"${key}\": $(cat "${file}")," >> "${output}"
}

### Fetch some artifacts that won't be attached to the data to be sent to ElasticSearch
fetchAndDefault 'steps-info.json' "${BO_BUILD_URL}/steps/" "${DEFAULT_HASH}"
fetchAndDefault 'pipeline-log.txt' "${BO_BUILD_URL}/log/" '" "'
### Prepare the log summary
if [ -e pipeline-log.txt ] ; then
    grep -v '\[Pipeline\]'  pipeline-log.txt | tail -n 100 > pipeline-log-summary.txt
fi

### Prepare build report file
echo '{' > "${BUILD_REPORT}"
fetchAndPrepareBuildReport 'job-info.json' "${BO_JOB_URL}/" "job" "${DEFAULT_HASH}"
fetchAndPrepareBuildReport 'tests-summary.json' "${BO_BUILD_URL}/blueTestSummary/" "test_summary" "${DEFAULT_LIST}"
fetchAndPrepareBuildReport 'tests-info.json' "${BO_BUILD_URL}/tests/?limit=100000000" "test" "${DEFAULT_LIST}"
fetchAndPrepareBuildReport 'changeSet-info.json' "${BO_BUILD_URL}/changeSet/" "changeSet" "${DEFAULT_LIST}"
fetchAndPrepareBuildReport 'artifacts-info.json' "${BO_BUILD_URL}/artifacts/" "artifacts" "${DEFAULT_LIST}"
fetchAndPrepareBuildInfo "${BUILD_INFO}" "${BO_BUILD_URL}/" "build" "${DEFAULT_HASH}"
echo '}' >> "${BUILD_REPORT}"

### Pretty
prettyJson "${BUILD_INFO}"
prettyJson "${BUILD_REPORT}"

exit $STATUS
