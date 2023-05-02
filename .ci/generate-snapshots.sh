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
# It produces the list of active release branches using the artifacts-api and does some
# manipulation to discard those branches that are not active anymore. A bit opinionated.
#
set -eo pipefail

## We avoid surprises by uploading the unexpected credentials json file
mkdir snapshots
cd snapshots
URL="https://artifacts-api.elastic.co/v1"
NO_KPI_URL_PARAM="x-elastic-no-kpi=true"


echo ">> Query versions in ${URL}"
QUERY_OUTPUT=$(curl -s "${URL}/versions?${NO_KPI_URL_PARAM}"| jq -r '.aliases[] | select(contains("SNAPSHOT"))')
for version in ${QUERY_OUTPUT}; do
  LATEST_OUTPUT=$(curl -s "${URL}/versions/${version}/builds/latest?${NO_KPI_URL_PARAM}" | jq 'del(.build.projects,.manifests) | . |= .build')
  BRANCH=$(echo "$LATEST_OUTPUT" | jq -r .branch)
  echo "${LATEST_OUTPUT}" | tee "$BRANCH.json"
done

echo ">> Support master branch"
cp master.json main.json || true

## Generate a manifest with the current active snapshot branches (it also includes main).
## Aka those with artifacts that have been generated in the last 30 days.
echo ">> Query branches in ${URL}"
BRANCHES=$(curl -s "${URL}/versions?${NO_KPI_URL_PARAM}" | jq -r 'del(.aliases[] | select(test("SNAPSHOT$")|not)) | .aliases' | jq '. + [ "main" ]' | sed 's#-SNAPSHOT##g')
{
  echo "{"
  echo "\"branches\":"
  echo "${BRANCHES}"
  echo "}"
} > branches.json

echo ">> Remove branches that have not been created yet"
## Remove branches that have not been created yet, for such it queries the GitHub repositories
for branch in $(jq -r '.branches | .[]' branches.json); do
  if git ls-remote --exit-code --heads https://github.com/elastic/elasticsearch.git "$branch" > /dev/null ; then
    echo ">>> $branch exists"
  else
    ## fallback to kibana just in case
    if git ls-remote --exit-code --heads https://github.com/elastic/kibana.git "$branch" > /dev/null ; then
      echo ">>> $branch exists"
    else
      echo ">>> $branch does not exist ... let's remove it"
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

echo ">> No more than 2 versions for the same minor is needed"
if [ "$(jq -r '.branches | map(select(. | startswith("8."))) | length' branches.json)" == "3" ] ; then
  echo ">>> Remove the first element"
  removeFirstElement=$(jq -r '.branches | map(select(. | startswith("8."))) | .[0]' branches.json)
  export removeFirstElement
  echo ">>> $removeFirstElement"
  jq -r 'del(.branches[] | select(test(env.removeFirstElement)))' branches.json > branches.json.tmp
  mv branches.json.tmp branches.json
fi

echo ">> Can 2 versions for the same minor be available?"
## There are times when there are two minor versions at the same time and that's valid in some cases but
## in other cases it's not required.
searchLatestBranch=$(jq -r '.branches | map(select(. != "main")) | .[-1]' branches.json)

## If branch is not available yet, likely it's related when a new release is created from the main branch
## then the unified release likely has not been triggered yet. Then let's fall back to create the file
## matching the main.json (this will avoid issues with the consumers)
if [ ! -e  "$searchLatestBranch.json" ] ; then
  cp main.json "$searchLatestBranch.json"
fi

searchVersion=$(jq -r '.version' "$searchLatestBranch.json" | sed 's#-SNAPSHOT##g')
if [ "${searchVersion}" != "${searchLatestBranch}.0"  ] ; then
  echo ">>> Remove <major>.x-1"
  ## Remove 8.x-1
  majorVersion=$(cut -d '.' -f 1 <<< "$searchLatestBranch")
  minorVersion=$(cut -d '.' -f 2 <<< "$searchLatestBranch")
  ## manipulate minorVersion to get the -1
  newMinorVersion=$(echo "$minorVersion - 1" | bc)
  export removeBranch="${majorVersion}.${newMinorVersion}"
  echo ">>> $removeBranch"
  jq -r 'del(.branches[] | select(test(env.removeBranch)))' branches.json > branches.json.tmp
  mv branches.json.tmp branches.json
fi
