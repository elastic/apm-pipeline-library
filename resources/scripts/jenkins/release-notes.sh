#!/usr/bin/env bash
set -uxeo pipefail

GREN_GITHUB_TOKEN=${GREN_GITHUB_TOKEN:?"missing GREN_GITHUB_TOKEN"}

gren release --token="${GREN_GITHUB_TOKEN}" -c .grenrc.js -t all
# it is generated from scratch to have reverse version order
gren changelog --token="${GREN_GITHUB_TOKEN}" --override -c .grenrc.js -t all -G
