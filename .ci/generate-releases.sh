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
# It produces the list of current releases, BCs or upcoming ones using the artifacts-api and GitHub API.
# Then it does some manipulation to discard those releases that are not available. A bit opinionated.
#
set -eo pipefail

# Get the latest github release for the given tag prefix.
# Releases starts with v<major>, i.e: v8
# It uses the gh cli in elastic/elasticsearch.
# Owned by: release team
function latest() {
  local version="${1}"
  gh api repos/elastic/elasticsearch/releases \
    | jq -r --arg version "$version" '[.[].tag_name
    | select(startswith($version))
    | sub("v"; ""; "g")]
    | sort_by(.| split(".") | map(tonumber))
    | .[-1]'
}

# Get the next release for the given semver prefix.
# Releases start with major.minor.patch.
# It uses the artifacts-api.elastic.co entrypoint.
# Owned by: release team
function next() {
  local version="${1}"
  local URL="https://artifacts-api.elastic.co/v1"
  local NO_KPI_URL_PARAM="x-elastic-no-kpi=true"
  curl -s "${URL}/versions?${NO_KPI_URL_PARAM}" \
    | jq -r --arg version "$version" '[.versions[]
    | select(contains("SNAPSHOT")|not)
    | select(startswith($version))]
    | sort_by(.| split(".") | map(tonumber))
    | .[-1]'
}

# Bump the patch version for the given version
function incPatch() {
  local version="$1"
  version="${version#[vV]}"
  major="${version%%\.*}"
  minor="${version#*.}"
  minor="${minor%.*}"
  vpatch="${version##*.}"
  echo "${major}.${minor}.$((vpatch + 1))"
}

# Get the latest version in the main branch.
# It uses the https://storage.googleapis.com/artifacts-api.
# Owned by: observability-robots
function edge() {
  curl -s https://storage.googleapis.com/artifacts-api/snapshots/main.json \
  | jq -r .build_id \
  | sed 's#-.*##g'
}

# Get the major version for the given semver
function major() {
  local version="$1"
  version="${version#[vV]}"
  major="${version%%\.*}"
  echo "${major}"
}

# Get the major.minor version for the given semver
function majorminor() {
  local version="$1"
  version="${version#[vV]}"
  major="${version%%\.*}"
  minor="${version#*.}"
  minor="${minor%.*}"
  echo "${major}.${minor}"
}

## Static versions
current_6="6.8.23"

## Fetch the versions
current_7=$(latest v7)
current_8=$(latest v8)
next7=$(incPatch "$current_7")
next8=$(next 8)
patch8=$(incPatch "$current_8")
edge_8=$(edge)

## Validate if releases are available

## Generate files

### We avoid surprises by uploading the unexpected credentials json file
mkdir releases
cd releases

{
  echo "current_6=$current_6"
  echo "current_7=$current_7"
  echo "next_minor_7=$next7"
  echo "next_patch_7=$next7"
  echo "current_8=$current_8"
  echo "next_minor_8=$next8"
  echo "next_patch_8=$patch8"
  echo "edge_8=$edge_8"
} | tee releases.properties

mkdir -p releases/current
echo "$current_6" > "releases/current/$(major "$current_6")"
echo "$current_7" > "releases/current/$(major "$current_7")"
echo "$current_8" > "releases/current/$(major "$current_8")"
echo "$current_6" > "releases/current/$(majorminor "$current_6")"
echo "$current_7" > "releases/current/$(majorminor "$current_7")"
echo "$current_8" > "releases/current/$(majorminor "$current_8")"

mkdir -p releases/next
echo "$next7" > "releases/next/minor-$(major "$next7")"
echo "$next8" > "releases/next/minor-$(major "$next8")"
echo "$next7" > "releases/next/patch-$(major "$next7")"
echo "$patch8" > "releases/next/patch-$(major "$patch8")"

mkdir -p releases/edge
echo "$edge_8" > "releases/edge/$(major "$edge_8")"
