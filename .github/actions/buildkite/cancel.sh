#!/usr/bin/env bash
#
# Cancel the running build
#
set -euo pipefail

MSG="parameter missing."
ORG=${1:?$MSG}
PIPELINE=${2:?$MSG}
NUMBER=${3:?$MSG}
BK_TOKEN=${4:?$MSG}

curl \
  --no-progress-meter \
  -H "Authorization: Bearer $BK_TOKEN" \
  -X "PUT" \
  "https://api.buildkite.com/v2/organizations/$ORG/pipelines/$PIPELINE/builds/$NUMBER/cancel"
