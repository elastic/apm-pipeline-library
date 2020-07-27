#!/usr/bin/env bash
set -eo pipefail

## See https://github.com/github/super-linter/blob/master/docs/run-linter-locally.md
docker run \
    -ti \
    --rm \
    -e RUN_LOCAL=true \
    -e OUTPUT_FORMAT=tap \
    -e OUTPUT_DETAILS=detailed \
    -v "$(pwd)":/tmp/lint \
    github/super-linter:latest
