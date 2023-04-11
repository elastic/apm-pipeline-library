#!/usr/bin/env bash
#
# Given some parameters it will trigger a build in Jenkins
#
# Parameters:
#  $1 -> the Jenkins URL. Mandatory.
#  $2 -> the Jenkins job. Mandatory.
#  $3 -> the build params. Mandatory. "" if empty
#  $4 -> whether to wait for. Mandatory.
#  $5 -> whether to report logs. Mandatory
#  $6 -> the Jenkins API token. Mandatory.
#

set -euo pipefail

MSG="parameter missing."
URL=${1:?$MSG}
JOB=${2:?$MSG}
BUILD_PARAMS=${3:-''}
WAIT_FOR=${4:?$MSG}
PRINT_BUILD=${5:?$MSG}
JENKINS_TOKEN=${6:?$MSG}

# Merge in the build environment variables, if they specified any
BUILD_PARAMS_MANIPULATED=""
if [[ -n "$BUILD_PARAMS" ]]; then
  # Parse those env variables that are split in lines (VARIABLE=value)
  while IFS= read -r line; do
    if [ -n "$line" ] ; then
      name=$(echo "$line" | cut -d= -f1)
      value=$(echo "$line" | cut -d= -f2)
      BUILD_PARAMS_MANIPULATED="${BUILD_PARAMS_MANIPULATED} -p $name=$value"
    fi
  done <<< "$BUILD_PARAMS"
fi

FLAGS=''
if [ "$WAIT_FOR" == "true" ]; then
  FLAGS="-s"
  if [ "$PRINT_BUILD" == "true" ]; then
    FLAGS="${FLAGS} -v"
  fi
fi

echo "::group::Download Jenkins client"
curl --no-progress-meter \
  -s "${URL}/jnlpJars/jenkins-cli.jar" \
  --output jenkins-cli.jar
echo "::endgroup::"

echo "::group::BuildLogs"
# shellcheck disable=SC2086
java -jar \
  jenkins-cli.jar \
  -s "${URL}" \
  -auth "${JENKINS_TOKEN}" \
  -webSocket \
  build \
  "${JOB}" \
  -w \
  ${FLAGS} \
  $BUILD_PARAMS_MANIPULATED
echo "::endgroup::"
