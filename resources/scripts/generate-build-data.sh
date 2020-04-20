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

restURLJob=$1
restURLBuild=$2
RESULT=$3
DURATION=$4

### Functions
function fetch() {
    file=$1
    url=$2
    #(retry 3 ${CURL_COMMAND} -o "${file}" "${url}") || status=1
    ${CURL_COMMAND} -o "${file}" "${url}" || status=1
}

function fetchAndPrepareBuildInfo() {
    output="build-info.json"
    fetchAndPrepare "$1" "$2" "$3" "$4" "${output}"

    tmp=$(mktemp)
    jq --arg a "${RESULT}" '.build.result = $a' "${output}" > "$tmp" && mv "$tmp" "${output}"
    jq --arg a "${DURATION}" '.build.durationInMillis = $a' "${output}" > "$tmp" && mv "$tmp" "${output}"
    jq '.build.state = "FINISHED"' "${output}" > "$tmp" && mv "$tmp" "${output}"
}

function fetchAndPrepareBuildReport() {
    fetchAndPrepare $1 $2 $3 $4 "build-report.json"
}

function fetchAndPrepare() {
    file=$1
    url=$2
    key=$3
    default=$4
    output=$5

    fetch "${file}" "${url}"

    if [ -e ${file} ] ; then
        echo "\"${key}\": $(cat ${file})," >> ${output}
    else
        if [ "${default}" == 'hash' ] ; then
            echo "\"${key}\": { }," >> ${output}
        else
            echo "\"${key}\": [ ]," >> ${output}
        fi
    fi
}

CURL_COMMAND="curl -sfS --max-time 60 --connect-timeout 30"

if [ -e '/usr/local/bin/bash_standard_lib.sh' ] ; then
    source /usr/local/bin/bash_standard_lib.sh
fi

## To report the status afterwards
status=0

### Query entrypoints
fetch 'build-info.json' "${restURLBuild}/"
fetch 'tests-summary.json' "${restURLBuild}/blueTestSummary/"
fetch 'tests-info.json' "${restURLBuild}/tests/?limit=100000000"
fetch 'changeSet-info.json' "${restURLBuild}/changeSet/"
fetch 'artifacts-info.json' "${restURLBuild}/artifacts/"
fetch 'steps-info.json' "${restURLBuild}/steps/"
fetch 'pipeline-log.txt' "${restURLBuild}/log/"

### Prepare build info file
echo '{' > build-info.json
fetchAndPrepareBuildInfo 'build-data.json' "${restURLBuild}/" "build" "hash"
echo '}' >> build-info.json
### Prepare build report file
echo '{' >> build-report.json
fetchAndPrepareBuildReport 'job-info.json' "${restURLJob}/" "job" "hash"
fetchAndPrepareBuildReport 'tests-summary.json' "${restURLBuild}/blueTestSummary/" "test_summary" "list"
fetchAndPrepareBuildReport 'tests-info.json' "${restURLBuild}/tests/?limit=100000000" "test" "list"
fetchAndPrepareBuildReport 'changeSet-info.json' "${restURLBuild}/changeSet/" "changeSet" "list"
fetchAndPrepareBuildReport 'artifacts-info.json' "${restURLBuild}/artifacts/" "artifacts" "list"
fetchAndPrepareBuildReport 'steps-info.json' "${restURLBuild}/steps/" "steps" "list"
cat build-info.json | sed '1d;$d' >> build-report.json
fetchAndPrepareBuildReport 'pipeline-log.txt' "${restURLBuild}/log/" "log" "hash"
echo '}' >> build-report.json

### Remove last ocurrence for the items separator
sed -i bck 's/\(.*\),/\1 /' build-report.json

### Prepare the log summary
if [ -e pipeline-log.txt ] ; then
    tail -n 100 pipeline-log.txt > pipeline-log-summary.txt
fi

exit $status
