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
#  $7 -> the sha commit. Mandatory
#  $8 -> the author. Mandatory
#  $9 -> the commit message. Mandatory.
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
COMMIT=${7:?$MSG}
AUTHOR=${8:-"unknown"}
MESSAGE=${9:-"Triggered by the GitHub action"}

JSON=$(
jq -c -n \
  --arg COMMIT  "$COMMIT" \
  --arg BRANCH  "main" \
  --arg COMMIT_AUTHOR "$AUTHOR" \
  --arg COMMIT_MSG "$MESSAGE" \
  '{
    "commit": $COMMIT,
    "branch": $BRANCH,
    "author": {
      "name": $COMMIT_AUTHOR,
    },
    "message": $COMMIT_MSG,
    "ignore_pipeline_branch_filters": true
  }'
)

# Merge in the build environment variables, if they specified any
if [[ -n "$BUILD_VARS" ]]; then
  if ! JSON=$(echo "$JSON" | jq -c --argjson BUILD_ENV_VARS "$BUILD_VARS" '. + {env: $BUILD_ENV_VARS}'); then
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

if [ "$WAIT_FOR" != "true" ]; then
  echo "No wait for"
  exit 0
fi

echo "::group::WaitFor"
URL=$(echo "$RESP" | jq -r ".url")
WEB_URL=$(echo "$RESP" | jq -r ".web_url")
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
  exit 0
else
  echo "Build did not pass, it's '$STATE'. Check the logs at $WEB_URL"
  exit 1
fi
