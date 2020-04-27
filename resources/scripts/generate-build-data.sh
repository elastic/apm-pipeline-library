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
ARTIFACTS_INFO="artifacts-info.json"
BUILD_INFO="build-info.json"
BUILD_REPORT="build-report.json"
CHANGESET_INFO="changeSet-info.json"
JOB_INFO="job-info.json"
PIPELINE_LOG="pipeline-log.txt"
STEPS_ERRORS="steps-errors.json"
STEPS_INFO="steps-info.json"
TESTS_ERRORS="tests-errors.json"
TESTS_INFO="tests-info.json"
TESTS_SUMMARY="tests-summary.json"
UTILS_LIB='/usr/local/bin/bash_standard_lib.sh'

DEFAULT_HASH="{ }"
DEFAULT_LIST="[ ]"
DEFAULT_STRING='" "'

### Prepare the utils for the context if possible
if [ -e "${UTILS_LIB}" ] ; then
    # shellcheck disable=SC1091,SC1090
    source "${UTILS_LIB}"
fi

### Prepare jq for this context
if [ ! -x "$(command -v jq)" ] ; then
    tmp=$(mktemp -d)

    if [[ "$OSTYPE" == "darwin"* ]]; then
        file='jq-osx-amd64'
    else
        file='jq-linux64'
    fi
    wget -q -O "${tmp}/jq" https://github.com/stedolan/jq/releases/download/jq-1.6/${file}
    chmod 755 "${tmp}/jq"
    PATH=${tmp}:${PATH}
fi

### Functions
function sedCommand() {
    flag="-i"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        flag="-i ''"
    fi
    sed "${flag}" "$@"
}

function curlCommand() {
    curl --silent --max-time 600 --connect-timeout 30 -o "$1" "$2"
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

    echo "INFO: fetchAndPrepareBuildInfo (see ${file})"
    fetchAndDefault "${file}" "${url}" "${default}"

    normaliseBuild "${file}"

    echo "\"${key}\": $(cat "${file}")" >> "${BUILD_REPORT}"
}

function fetchAndPrepareBuildReport() {
    fetchAndPrepare "$1" "$2" "$3" "$4" "${BUILD_REPORT}"
}

function fetchAndPrepareTestsInfo() {
    file=$1
    url=$2
    key=$3
    default=$4

    echo "INFO: fetchAndPrepareTestsInfo (see ${file})"
    fetchAndDefault "${file}" "${url}" "${default}"
    normaliseTests "${file}"

    echo "\"${key}\": $(cat "${file}")," >> "${BUILD_REPORT}"
}

function fetchAndPrepareArtifactsInfo() {
    file=$1
    url=$2
    key=$3
    default=$4

    echo "INFO: fetchAndPrepareArtifactsInfo (see ${file})"
    fetchAndDefault "${file}" "${url}" "${default}"
    normaliseArtifacts "${file}"

    echo "\"${key}\": $(cat "${file}")," >> "${BUILD_REPORT}"
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

function normaliseArtifacts() {
    file=$1
    jqEdit 'map(del(._links))' "${file}"
    jqEdit 'map(del(._class))' "${file}"
}

function normaliseBuild() {
    file=$1
    # shellcheck disable=SC2016
    jqAppend "${RESULT}" '.result = $a' "${file}"
    # shellcheck disable=SC2016
    jqAppend "${DURATION}" '.durationInMillis = ($a|tonumber)' "${file}"
    jqEdit '.state = "FINISHED"' "${file}"
}

function normaliseTests() {
    file=$1
    jqEdit 'map(del(._links))' "${file}"
    jqEdit 'map(del(._class))' "${file}"
    jqEdit 'map(del(.state))' "${file}"
    jqEdit 'map(del(.hasStdLog))' "${file}"
    ## This will help to tidy up the file size quite a lot.
    ## It might be useful to eexport it but let's go step by step
    jqEdit 'map(del(.errorStackTrace))' "${file}"
}

function normaliseSteps() {
    file=$1
    jqEdit 'map(del(._links))' "${file}"
    jqEdit 'map(del(._class))' "${file}"
    jqEdit 'map(del(.actions))' "${file}"
}

function fetchAndDefaultStepsInfo() {
    file=$1
    url=$2
    default=$3

    fetchAndDefault "${file}" "${url}" "${default}"
    normaliseSteps "${file}"

    ### Prepare steps errors report
    output="${STEPS_ERRORS}"
    jq 'map(select(.result=="FAILURE"))' "${file}" > "${output}"
    if ! grep  -q 'result' "${output}" ; then
        echo "${default}" > "${output}"
    fi
}

function fetchAndDefaultTestsErrors() {
    file=$1
    url=$2
    default=$3

    fetchAndDefault "${file}" "${url}" "${default}"
    normaliseTests "${file}"
}

function jqEdit() {
    query=$1
    file=$2
    tmp=$(mktemp)
    jq "${query}" "${file}" > "$tmp" && mv "$tmp" "${file}"
}

function jqAppend() {
    argument=$1
    query=$2
    file=$3
    tmp=$(mktemp)
    jq --arg a "${argument}" "${query}" "${file}" > "$tmp" && mv "$tmp" "${file}"
}

### Fetch some artifacts that won't be attached to the data to be sent to ElasticSearch
fetchAndDefaultStepsInfo "${STEPS_INFO}" "${BO_BUILD_URL}/steps/?limit=10000" "${DEFAULT_HASH}"
fetchAndDefaultTestsErrors "${TESTS_ERRORS}" "${BO_BUILD_URL}/tests/?status=FAILED" "${DEFAULT_LIST}"
fetchAndDefault "${PIPELINE_LOG}" "${BO_BUILD_URL}/log/" "${DEFAULT_STRING}"

### Prepare the log summary
if [ -e "${PIPELINE_LOG}" ] ; then
    grep -v '\[Pipeline\]' "${PIPELINE_LOG}" | tail -n 100 > pipeline-log-summary.txt
fi

### Prepare build report file
echo '{' > "${BUILD_REPORT}"
fetchAndPrepareBuildReport "${JOB_INFO}" "${BO_JOB_URL}/" "job" "${DEFAULT_HASH}"
fetchAndPrepareBuildReport "${TESTS_SUMMARY}" "${BO_BUILD_URL}/blueTestSummary/" "test_summary" "${DEFAULT_LIST}"
fetchAndPrepareBuildReport "${CHANGESET_INFO}" "${BO_BUILD_URL}/changeSet/" "changeSet" "${DEFAULT_LIST}"
fetchAndPrepareArtifactsInfo "${ARTIFACTS_INFO}" "${BO_BUILD_URL}/artifacts/" "artifacts" "${DEFAULT_LIST}"
fetchAndPrepareTestsInfo "${TESTS_INFO}" "${BO_BUILD_URL}/tests/?limit=10000000" "test" "${DEFAULT_LIST}"
fetchAndPrepareBuildInfo "${BUILD_INFO}" "${BO_BUILD_URL}/" "build" "${DEFAULT_HASH}"
echo '}' >> "${BUILD_REPORT}"

exit $STATUS
