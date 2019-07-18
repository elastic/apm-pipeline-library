#!/usr/bin/env bash
set -uxeo pipefail

BRANCH_NAME=${BRANCH_NAME:?"missing BRANCH_NAME"}

git checkout -f "${BRANCH_NAME}"
./mvnw release:prepare release:perform --batch-mode -Darguments="-DskipTests=true --batch-mode"
