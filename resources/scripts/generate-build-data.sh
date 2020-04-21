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
#set -x

BO_JOB_URL=${1:?'Missing the Blue Ocean Job URL'}
BO_BUILD_URL=${2:?'Missing the Blue Ocean Build URL'}

## To report the status afterwards
STATUS=0
BUILD_INFO_OBJECT="build-info-object.json"
BUILD_INFO="build-info.json"
BUILD_REPORT="build-report.json"

### Functions
function fetch() {
    file=$1
    url=$2
    
    ## Let's support retry in the CI.
    if [ -n "${JENKINS_URL}" ] ; then
        (retry 3 "${CURL_COMMAND}" -o "${file}" "${url}") || STATUS=1
    else
        ${CURL_COMMAND} -o "${file}" "${url}" || STATUS=1
    fi
}

function fetchAndPrepareBuildInfoObject() {
    output="${BUILD_INFO_OBJECT}"
    if [ -e "${output}" ] ; then
        rm "${output}"
    fi
    fetchAndPrepare "$1" "$2" "$3" "$4" "${output}"
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

    fetch "${file}" "${url}"

    if [ -e "${file}" ] ; then
        echo "\"${key}\": $(cat "${file}")," >> "${output}"
    else
        if [ "${default}" == 'hash' ] ; then
            echo "\"${key}\": { }," >> "${output}"
        else
            echo "\"${key}\": [ ]," >> "${output}"
        fi
    fi
}

CURL_COMMAND="curl -sfS --max-time 60 --connect-timeout 30"

if [ -e '/usr/local/bin/bash_standard_lib.sh' ] ; then
    # shellcheck disable=SC1091
    source /usr/local/bin/bash_standard_lib.sh
fi

### Fetch some artifacts that won't be attached to the data to be sent to ElasticSearch
fetch 'steps-info.json' "${BO_BUILD_URL}/steps/"
fetch 'pipeline-log.txt' "${BO_BUILD_URL}/log/"
### Prepare the log summary
if [ -e pipeline-log.txt ] ; then
    tail -n 100 pipeline-log.txt > pipeline-log-summary.txt
fi

### Prepare build info file
fetchAndPrepareBuildInfoObject 'build-data.json' "${BO_BUILD_URL}/" "build" "hash"
### Remove last ocurrence for the items separator
sed -i .bck 's/\(.*\),/\1 /' "${BUILD_INFO_OBJECT}"
echo '{' > "${BUILD_INFO}"
cat "${BUILD_INFO_OBJECT}" >> "${BUILD_INFO}"
echo '}' >> "${BUILD_INFO}"

### Prepare build report file
echo '{' > "${BUILD_REPORT}"
fetchAndPrepareBuildReport 'job-info.json' "${BO_JOB_URL}/" "job" "hash"
fetchAndPrepareBuildReport 'tests-summary.json' "${BO_BUILD_URL}/blueTestSummary/" "test_summary" "list"
fetchAndPrepareBuildReport 'tests-info.json' "${BO_BUILD_URL}/tests/?limit=100000000" "test" "list"
fetchAndPrepareBuildReport 'changeSet-info.json' "${BO_BUILD_URL}/changeSet/" "changeSet" "list"
fetchAndPrepareBuildReport 'artifacts-info.json' "${BO_BUILD_URL}/artifacts/" "artifacts" "list"
cat "${BUILD_INFO_OBJECT}" >> "${BUILD_REPORT}"
echo '}' >> "${BUILD_REPORT}"

### Clean unrequired files
rm "${BUILD_INFO_OBJECT}" "${BUILD_INFO_OBJECT}.bck"

exit $STATUS
