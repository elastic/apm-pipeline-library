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

if [ -n "$RUNNER_DEBUG" ] ; then
  set -x
fi

###############
### FUNCTIONS
###############

# Retry the given command for the given times.
# It should not print anything if it failed to help
# the other functions that return values through the stdout.
retry() {
  local retries=$1
  shift
  local count=0
  until "$@"; do
    exit=$?
    wait=$((2 ** count))
    count=$((count + 1))
    if [ $count -lt "$retries" ]; then
      sleep $wait
    else
      return $exit
    fi
  done
  return 0
}

# Get the latest github release for the given tag prefix.
# Releases starts with v<major>, i.e: v8
# It uses the gh cli in elastic/elasticsearch.
# Owned by: release team
function latest() {
  local version="${1}"
  local file=.releases
  retry 3 gh api repos/elastic/elasticsearch/releases 2> /dev/null > $file
  jq -r --arg version "$version" '[.[].tag_name
    | select(startswith($version))
    | sub("v"; ""; "g")]
    | sort_by(.| split(".") | map(tonumber))
    | .[-1]' .releases
  rm $file &> /dev/null || true
}

function debug_latest() {
  local version="${1}"
  local file=.releases
  retry 3 gh api repos/elastic/elasticsearch/releases > $file
  jq -r --arg version "$version" '[.[].tag_name
    | select(startswith($version))
    | sub("v"; ""; "g")]
    | sort_by(.| split(".") | map(tonumber))' .releases
  rm $file &> /dev/null || true
}

# Get the next release for the given semver prefix.
# Releases start with major.minor.patch.
# It uses the artifacts-api.elastic.co entrypoint.
# Owned by: release team
function next() {
  local version="${1}"
  local file=.versions
  local URL="https://artifacts-api.elastic.co/v1"
  local NO_KPI_URL_PARAM="x-elastic-no-kpi=true"
  retry 3 curl -s "${URL}/versions?${NO_KPI_URL_PARAM}" 2> /dev/null > $file
  jq -r --arg version "$version" '[.versions[]
    | select(contains("SNAPSHOT")|not)
    | select(startswith($version))]
    | sort_by(.| split(".") | map(tonumber))
    | .[-1]' $file
  rm $file &> /dev/null || true
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
  local file=.edge
  retry 3 curl -s https://storage.googleapis.com/artifacts-api/snapshots/main.json 2> /dev/null > $file
  jq -r .build_id .edge | sed 's#-.*##g'
  rm $file &> /dev/null || true
}

# Get the major version for the given semver
function major() {
  local version="$1"
  version="${version#[vV]}"
  major="${version%%.*}"
  echo "${major}"
}

# Get the major.minor version for the given semver
function majorminor() {
  local version="$1"
  version="${version#[vV]}"
  major="${version%%.*}"
  minor="${version#*.}"
  minor="${minor%.*}"
  echo "${major}.${minor}"
}

# Whether the given version is available.
# It uses docker images in the internal docker registry
function isAvailable() {
  local version="$1"
  # apm-server docker image is smaller.
  if retry 3 docker pull --quiet docker.elastic.co/apm/apm-server:"$version"-SNAPSHOT &> /dev/null; then
    echo 'true'
  else
    # Fallback to use the elasticsearch - a quite  bigger docker image.
    if retry 3 docker pull --quiet docker.elastic.co/elasticsearch/elasticsearch:"$version"-SNAPSHOT &> /dev/null; then
      echo 'true'
    else
      echo 'false'
    fi
  fi
}

###############
### MAIN
###############

## 0. Static versions
current_6="6.8.23"

## 1. Fetch the versions
current_7=$(latest v7)
current_8=$(latest v8)
next_7=$(incPatch "$current_7")
next_minor_8=$(next 8)
next_patch_8=$(incPatch "$current_8")
edge_8=$(edge)

## debug
debug_latest v7
debug_latest v8

## 2. Generate files

### We avoid surprises by uploading the unexpected credentials json file
mkdir releases
cd releases

### IMPORTANT:
### This file might contain some versions that are not available yet.
### One way to solve this particular case will be by reading the current releases.properties
### and apply some validations with isAvailable, otherwise then fallback to the previous version.
{
  echo "current_6=$current_6"
  echo "current_7=$current_7"
  echo "next_minor_7=$next_7"
  echo "next_patch_7=$next_7"
  echo "current_8=$current_8"
  echo "next_minor_8=$next_minor_8"
  echo "next_patch_8=$next_patch_8"
  echo "edge_8=$edge_8"
  echo "generated=https://github.com/elastic/apm-pipeline-library/actions/workflows/generate-elastic-stack-releases.yml"
} | tee releases.properties

### Generate the files for the current releases
CURRENT_FOLDER=releases/current
mkdir -p $CURRENT_FOLDER
echo "$current_6" > "$CURRENT_FOLDER/$(major "$current_6")"
echo "$current_7" > "$CURRENT_FOLDER/$(major "$current_7")"
echo "$current_8" > "$CURRENT_FOLDER/$(major "$current_8")"
echo "$current_6" > "$CURRENT_FOLDER/$(majorminor "$current_6")"
echo "$current_7" > "$CURRENT_FOLDER/$(majorminor "$current_7")"
echo "$current_8" > "$CURRENT_FOLDER/$(majorminor "$current_8")"

### Generate the files for the upcoming releases only if artifacts
### are available
NEXT_FOLDER=releases/next
mkdir -p $NEXT_FOLDER
if [ "$(isAvailable "$next_7")" = "true" ] ; then
  echo "$next_7" > "$NEXT_FOLDER/minor-$(major "$next_7")"
  echo "$next_7" > "$NEXT_FOLDER/minor-$(majorminor "$next_7")"
  echo "$next_7" > "$NEXT_FOLDER/patch-$(majorminor "$next_7")"
  echo "$next_7" > "$NEXT_FOLDER/patch-$(major "$next_7")"
fi

if [ "$(isAvailable "$next_minor_8")" = "true" ] ; then
echo "$next_minor_8" > "$NEXT_FOLDER/minor-$(majorminor "$next_minor_8")"
echo "$next_minor_8" > "$NEXT_FOLDER/minor-$(major "$next_minor_8")"
fi

if [ "$(isAvailable "$next_patch_8")" = "true" ] ; then
  echo "$next_patch_8" > "$NEXT_FOLDER/patch-$(major "$next_patch_8")"
  echo "$next_patch_8" > "$NEXT_FOLDER/patch-$(majorminor "$next_patch_8")"
fi

EDGE_FOLDER=releases/edge
mkdir -p $EDGE_FOLDER
if [ "$(isAvailable "$edge_8")" = "true" ] ; then
  echo "$edge_8" > "$EDGE_FOLDER/$(major "$edge_8")"
  # NOTE: when 9.x happens then `main` will point to 9.
  echo "$edge_8" > "$EDGE_FOLDER/main"
fi
