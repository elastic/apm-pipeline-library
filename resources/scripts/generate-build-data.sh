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
BUILD_BULK_REPORT="build-report-bulk.json"
CHANGESET_INFO="changeSet-info.json"
ENV_INFO="env-info.json"
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

### To manipulate the steps
BASE_URL="${JENKINS_URL}"
if [ -z "${JENKINS_URL}" ] ; then
    BASE_URL=${BO_JOB_URL//\/blue\/rest\/*/}
fi

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
    curl --silent --max-time 600 --connect-timeout 30 -o "$1" "$2" --fail
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

    if [ ! -s "${file}" ] ; then
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

    echo "\"${key}\": $(cat "${file}")," >> "${BUILD_REPORT}"
}

function fetchAndPrepareBuildReport() {
    file=$1
    url=$2
    key=$3
    default=$4

    fetchAndDefault "${file}" "${url}" "${default}"
    normaliseArtifacts "${file}"
    normaliseBuildReport "${file}"
    normaliseChangeset "${file}"
    echo "\"${key}\": $(cat "${file}")," >> "${BUILD_REPORT}"
}

function fetchAndPrepareTestsInfo() {
    file=$1
    url=$2
    key=$3
    default=$4

    echo "INFO: fetchAndPrepareTestsInfo (see ${file})"
    fetchAndDefault "${file}" "${url}" "${default}"

    ## Tests json response differs when there were tests executed in
    ## the pipeline, otherwise it returns:
    ##   { message: "no tests", code: 404, errors: [] }
    if jq -e 'select(.code==404)' "${file}" > /dev/null 2>&1 ; then
        echo "${default}" > "${file}"
    else
        normaliseTestsWithoutStacktrace "${file}"
    fi

    echo "\"${key}\": $(cat "${file}")," >> "${BUILD_REPORT}"
}

function fetchAndPrepareTestSummaryReport() {
    file=$1
    url=$2
    key=$3
    default=$4
    testsFile=$5

    echo "INFO: fetchAndPrepareTestSummaryReport (see ${file})"
    fetch "$file" "$url"
    normaliseTestsSummary "$file"

    ## BlueOcean might return 500 in some scenarios. If so, let's parse the tests entrypoint
    if [ ! -e "${file}" ] ; then
        if [ -e "${testsFile}" ] ; then
            {
                echo "{"
                echo "\"total\": $(jq '. | length' "${testsFile}"),"
                echo "\"passed\": $(jq 'map(select(.status |contains("PASSED"))) | length' "${testsFile}"),"
                echo "\"failed\": $(jq 'map(select(.status |contains("FAILED"))) | length' "${testsFile}"),"
                echo "\"skipped\": $(jq 'map(select(.status |contains("SKIPPED"))) | length' "${testsFile}")"
                echo "}"
            } > "${file}"
        else
            echo "${default}" > "${file}"
        fi
    fi

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
    jqEdit 'map(del(.downloadable))' "${file}"
    jqEdit 'map(del(.id))' "${file}"
    jqEdit 'map(del(.url))' "${file}"
}

function normaliseBuild() {
    file=$1
    # shellcheck disable=SC2016
    jqAppend "${RESULT}" '.result = $a' "${file}"
    # shellcheck disable=SC2016
    jqAppend "${DURATION}" '.durationInMillis = ($a|tonumber)' "${file}"
    jqEdit '.state = "FINISHED"' "${file}"
    jqEdit 'del(._links)' "${file}"
    jqEdit 'del(._class)' "${file}"
    ## This is already duplicated, the responsible is the job
    jqEdit 'del(.branch)' "${file}"
    ## This is already duplicated, the responsible is the changeset
    jqEdit 'del(.changeSet)' "${file}"
    ## This is already duplicated, the responsible is the job
    jqEdit 'del(.pullRequest)' "${file}"
    jqEdit 'del(.causes[]._class)' "${file}"
    jqEdit 'del(.replayable)' "${file}"

    ## Transform relative path to absolute URL
    artifactsZipFile=$(jq -r '.artifactsZipFile' "${file}")
    # shellcheck disable=SC2016
    jqAppend "${JENKINS_URL}${artifactsZipFile}" '.artifactsZipFile = $a' "${file}"
}

function normaliseBuildReport() {
    file=$1
    jqEdit 'del(._links)' "${file}"
    jqEdit 'del(._class)' "${file}"
    jqEdit 'del(.latestRun)' "${file}"
    jqEdit 'del(.permissions)' "${file}"
    jqEdit 'del(.parameters)' "${file}"
}

function normaliseChangeset() {
    file=$1
    jqEdit 'del(.[].author._class)' "${file}"
    jqEdit 'del(.[].author._links)' "${file}"
}

function normaliseTests() {
    file=$1
    jqEdit 'map(del(._links))' "${file}"
    jqEdit 'map(del(._class))' "${file}"
    jqEdit 'map(del(.state))' "${file}"
    jqEdit 'map(del(.hasStdLog))' "${file}"
}

function normaliseTestsWithoutStacktrace() {
    file=$1
    normaliseTests "${file}"
    ## This will help to tidy up the file size quite a lot.
    ## It might be useful to export it but lets go step by step
    jqEdit 'map(del(.errorStackTrace))' "${file}"
}

function normaliseTestsSummary() {
    file=$1
    jqEdit 'del(._links)' "${file}"
    jqEdit 'del(._class)' "${file}"
}

function normaliseSteps() {
    file=$1
    # shellcheck disable=SC2016
    jqAppend "${BASE_URL}" 'map(with_entries(select(.key != "_links")) + ( .url = $a + ._links.self.href + "log"))' "${file}"
    jqEdit 'map(del(._links))' "${file}"
    jqEdit 'map(del(._class))' "${file}"
    jqEdit 'map(del(.actions))' "${file}"
}

function fetchAndDefaultStepsInfo() {
    file=$1
    url=$2
    default=$3

    fetchAndDefault "${file}" "${url}" "${default}"

    ### Prepare steps errors report
    output="${STEPS_ERRORS}"
    jq 'map(select(.result=="FAILURE"))' "${file}" > "${output}"
    if ! grep  -q 'result' "${output}" ; then
        echo "${default}" > "${output}"
    else
         ### Update the displayDescription for those steps with a failure and an empty displayDescription.
         ###    For instance, when using the pipeline step `error('foo')`
         ###    then the 'foo' message is not shown in the BlueOcean restAPI.
         ###
        if jq -e 'map(select(.type=="STEP" and .result=="FAILURE" and .displayDescription==null))' "${output}" > /dev/null ; then
            tmp="$(mktemp -d)/step.log"
            for href in $(jq -r 'map(select(.type=="STEP" and .result=="FAILURE" and .displayDescription==null) | ._links.self.href) | .[]' "${output}"); do
                id=$(basename "${href}")
                new=$(curl -s "${BASE_URL}${href}log/" | head -c 100)
                curlCommand "${tmp}" "${BASE_URL}${href}log/"
                ## If the URL was unreachable then the file won't exist.
                ## For such use case, then avoid any transformation.
                if [ -e "${tmp}" ] ; then
                    new=$(head -c 100 "${tmp}")
                    jq --arg id "${id}" --arg new "${new}" '(.[] | select(.result=="FAILURE" and .displayDescription==null and .id==$id) | .displayDescription) |= $new' "${output}" > "$tmp" && mv "$tmp" "${output}"
                fi
            done
        fi
    fi

    ## Normalise later on
    normaliseSteps "${file}"
    normaliseSteps "${output}"
}

function fetchAndDefaultTestsErrors() {
    file=$1
    url=$2
    default=$3

    fetchAndDefault "${file}" "${url}" "${default}"

    ## Tests json response differs when there were tests executed in
    ## the pipeline, otherwise it returns:
    ##   { message: "no tests", code: 404, errors: [] }
    if jq -e 'select(.code==404)' "${file}" > /dev/null 2>&1 ; then
        echo "${default}" > "${file}"
    else
        normaliseTests "${file}"
    fi
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

function prepareEnvInfo() {
    file=$1
    key=$2

    {
        echo "{"
        echo "  \"BRANCH_NAME\": \"${BRANCH_NAME}\","
        echo "  \"BUILD_DISPLAY_NAME\": \"${BUILD_DISPLAY_NAME}\","
        echo "  \"BUILD_ID\": \"${BUILD_ID}\","
        echo "  \"BUILD_NUMBER\": \"${BUILD_NUMBER}\","
        echo "  \"BUILD_TAG\": \"${BUILD_TAG}\","
        echo "  \"BUILD_URL\": \"${BUILD_URL}\","
        echo "  \"GIT_BASE_COMMIT\": \"${GIT_BASE_COMMIT}\","
        echo "  \"GIT_COMMIT\": \"${GIT_COMMIT}\","
        echo "  \"GIT_PREVIOUS_COMMIT\": \"${GIT_PREVIOUS_COMMIT}\","
        echo "  \"GIT_PREVIOUS_SUCCESSFUL_COMMIT\": \"${GIT_PREVIOUS_SUCCESSFUL_COMMIT}\","
        echo "  \"JOB_BASE_NAME\": \"${JOB_BASE_NAME}\","
        echo "  \"JOB_DISPLAY_URL\": \"${JOB_DISPLAY_URL}\","
        echo "  \"JOB_NAME\": \"${JOB_NAME}\","
        echo "  \"JOB_URL\": \"${JOB_URL}\","
        echo "  \"ORG_NAME\": \"${ORG_NAME}\","
        echo "  \"REPO_NAME\": \"${REPO_NAME}\""
        echo "}"
    } > "${file}"

    ## This is the last entry in the BUILD_REPORT therefore no , is required
    echo "\"${key}\": $(cat "${file}")" >> "${BUILD_REPORT}"
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
fetchAndPrepareBuildReport "${CHANGESET_INFO}" "${BO_BUILD_URL}/changeSet/" "changeSet" "${DEFAULT_LIST}"
fetchAndPrepareArtifactsInfo "${ARTIFACTS_INFO}" "${BO_BUILD_URL}/artifacts/" "artifacts" "${DEFAULT_LIST}"
fetchAndPrepareTestsInfo "${TESTS_INFO}" "${BO_BUILD_URL}/tests/?limit=10000000" "test" "${DEFAULT_LIST}"
### fetchAndPrepareTestSummaryReport should run after fetchAndPrepareTestsInfo
fetchAndPrepareTestSummaryReport "${TESTS_SUMMARY}" "${BO_BUILD_URL}/blueTestSummary/" "test_summary" "${DEFAULT_LIST}" "${TESTS_INFO}"
fetchAndPrepareBuildInfo "${BUILD_INFO}" "${BO_BUILD_URL}/" "build" "${DEFAULT_HASH}"
### prepareEnvInfo should run the last one since it's the last field to be added
prepareEnvInfo "${ENV_INFO}" "env"
echo '}' >> "${BUILD_REPORT}"

### For each entry in the test map then create a flatten document
N=0
jq -c '.test = (.test[])' "${BUILD_REPORT}" |
while read -r json ; do
  N=$((N+1))
  echo "{ \"index\":{} }" >> "${BUILD_BULK_REPORT}"
  echo "{ \"doc\": ${json} }" >> "${BUILD_BULK_REPORT}"
done

exit $STATUS
