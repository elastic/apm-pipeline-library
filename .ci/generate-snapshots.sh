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
# It queries the artifacts-api entry point to fetch the next release
# version for the 8.x line.
#
set -eo pipefail

## We avoid surprises by uploading the unexpected credentials json file
mkdir snapshots
cd snapshots
URL="https://artifacts-api.elastic.co/v1"
NO_KPI_URL_PARAM="x-elastic-no-kpi=true"

QUERY_OUTPUT=$(curl -s "${URL}/versions?${NO_KPI_URL_PARAM}"| jq -r '.aliases[] | select(contains("SNAPSHOT"))')
for version in ${QUERY_OUTPUT}; do
  LATEST_OUTPUT=$(curl -s "${URL}/versions/${version}/builds/latest?${NO_KPI_URL_PARAM}" | jq 'del(.build.projects,.manifests) | . |= .build')
  BRANCH=$(echo "$LATEST_OUTPUT" | jq -r .branch)
  echo "${LATEST_OUTPUT}" | tee "$BRANCH.json"
done
## support main branch
cp master.json main.json || true

## generate a manifest with the current active snapshot branches (it also includes main).
## Aka those with artifacts that have been generated in the last 30 days.
BRANCHES=$(curl -s "${URL}/versions?${NO_KPI_URL_PARAM}" | jq -r 'del(.aliases[] | select(test("SNAPSHOT$")|not)) | .aliases' | jq '. + [ "main" ]' | sed 's#-SNAPSHOT##g')
{
  echo "{"
  echo "\"branches\":"
  echo "${BRANCHES}"
  echo "}"
} > branches.json

## Remove branches that have not been created yet, for such it queries the GitHub repositories
for branch in $(jq -r '.branches | .[]' branches.json); do
  if git ls-remote --exit-code --heads https://github.com/elastic/elasticsearch.git "$branch" ; then
    echo "$branch"
  else
    ## fallback to kibana just in case
    if git ls-remote --exit-code --heads https://github.com/elastic/kibana.git "$branch" ; then
      echo "$branch"
    else
      echo "$branch does not exist"
      {
        echo "{"
        echo "\"branches\":"
        jq ".branches - [\"$branch\"]" branches.json
        echo "}"
      } > branches.json.tmp
      mv branches.json.tmp branches.json
    fi
  fi
done

## There are times when there are two minor versions at the same time and that's valid in some cases but
## in other cases it's not required.
searchLatestBranch=$(jq -r '.branches | map(select(. != "main")) | .[-1]' branches.json)
searchVersion=$(jq -r '.version' "$searchLatestBranch.json" | sed 's#-SNAPSHOT##g')
if [ "${searchVersion}" != "${searchLatestBranch}.0"  ] ; then
  ## Remove 8.x-1
  majorVersion=$(cut -d '.' -f 1 <<< "$searchLatestBranch")
  minorVersion=$(cut -d '.' -f 2 <<< "$searchLatestBranch")
  ## manipulate minorVersion to get the -1
  newMinorVersion=$(echo "$minorVersion - 1" | bc)
  export removeBranch="${majorVersion}.${newMinorVersion}"
  jq -r 'del(.branches[] | select(test(env.removeBranch)))' branches.json > branches.json.tmp
  mv branches.json.tmp branches.json
fi
