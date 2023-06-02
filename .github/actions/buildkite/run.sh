#!/usr/bin/env bash
#
# Given some parameters it will trigger a build in Buidkite
#
# Parameters:
#  $1 -> the BK org. Mandatory.
#  $2 -> the BK pipeline. Mandatory.
#  $3 -> the build env vars in json format. Mandatory. "" if empty
#  $4 -> whether to wait for. Mandatory.
#  $5 -> whether to report logs. Mandatory
#  $6 -> the BK token. Mandatory.
#  $7 -> the BK build message. Mandatory.
#  $8 -> the Pipeline version. Mandatory.
#
# NOTE:
#  ignore_pipeline_branch_filters: By default Buildkite works only on master. As we want
#                                  to use different branch names, we have to set this.

set -euo pipefail

MSG="parameter missing."
ORG=${1:?$MSG}
PIPELINE=${2:?$MSG}
BUILD_VARS=${3:?$MSG}
WAIT_FOR=${4:?$MSG}
PRINT_BUILD=${5:?$MSG}
BK_TOKEN=${6:?$MSG}
MESSAGE=${7:-"Triggered automatically with GH actions"}
PIPELINE_VERSION=${8:-"HEAD"}

JSON=$(
jq -c -n \
  --arg COMMIT "$PIPELINE_VERSION" \
  --arg BRANCH "main" \
  --arg MESSAGE "$MESSAGE" \
  '{
    "commit": $COMMIT,
    "branch": $BRANCH,
    "message": $MESSAGE,
    "ignore_pipeline_branch_filters": true
  }'
)

# Merge in the build environment variables, if they specified any
if [[ -n "$BUILD_VARS" ]]; then
  # Parse those env variables that are split in lines (VARIABLE=value)
  BUILD_VARS_MANIPULATED="{"
  while IFS= read -r line; do
    if [ -n "$line" ] ; then
      name=$(echo "$line" | cut -d= -f1)
      value=$(echo "$line" | cut -d= -f2)
      BUILD_VARS_MANIPULATED="${BUILD_VARS_MANIPULATED} \"$name\": \"$value\","
    fi
  done <<< "$BUILD_VARS"
  BUILD_VARS_MANIPULATED="$(echo "$BUILD_VARS_MANIPULATED" | sed '$ s#,$##') }"
  if ! JSON=$(echo "$JSON" | jq -c --argjson BUILD_ENV_VARS "$BUILD_VARS_MANIPULATED" '. + {env: $BUILD_ENV_VARS}'); then
    echo ""
    echo "Error: BUILD_ENV_VARS provided invalid JSON: $BUILD_VARS"
    exit 1
  fi
fi

set +x
RESP=$(curl \
  --no-progress-meter \
  -H "Authorization: Bearer $BK_TOKEN" \
  "https://api.buildkite.com/v2/organizations/$ORG/pipelines/$PIPELINE/builds" \
  -X "POST" \
  -d "$JSON")

echo "::group::Output"
echo "Triggered build:"
echo "$RESP" | jq .
echo "::endgroup::"

URL=$(echo "$RESP" | jq -r ".url")
WEB_URL=$(echo "$RESP" | jq -r ".web_url")
BUILD_NUMBER=$(echo "$RESP" | jq -r ".number")
# shellcheck disable=SC2086
echo "build=$WEB_URL" >> $GITHUB_OUTPUT
echo "build_number=$BUILD_NUMBER" >> "$GITHUB_OUTPUT"
if [ "$WAIT_FOR" != "true" ]; then
  echo "No wait for build $WEB_URL to run "
  exit 0
fi

echo "::group::WaitFor"
STATE="running"

echo "Waiting for build $WEB_URL to run "
# https://buildkite.com/docs/pipelines/defining-steps#build-states
while [ "$STATE" == "running" ] || [ "$STATE" == "scheduled" ] || [ "$STATE" == "creating" ]; do
  RESP=$(curl \
    -H "Authorization: Bearer $BK_TOKEN" \
    --no-progress-meter \
    "$URL")
  STATE=$(echo "$RESP" | jq -r ".state")
  echo -n "."
  sleep 1
done
echo ""
echo "::endgroup::"

if [ "$PRINT_BUILD" == "true" ]; then
  echo "::group::BuildLogs"
  for logs_url in $(echo "$RESP" | jq -r ".jobs[].raw_log_url"); do
    echo "Fetching logs $logs_url"
    curl \
      -H "Authorization: Bearer $BK_TOKEN" \
      --no-progress-meter \
      "$logs_url"
  done
  echo "::endgroup::"
fi

if [ "$STATE" == "passed" ]; then
  echo "Build passed ($WEB_URL)"
  echo "build_state=${STATE}" >> "$GITHUB_OUTPUT"
  exit 0
else
  echo "Build did not pass, it's '$STATE'. Check the logs at $WEB_URL"
  echo "build_state=${STATE}" >> "$GITHUB_OUTPUT"
  exit 1
fi
