#!/usr/bin/env bash
set -eo pipefail

docker run --rm -t -v "${PWD}:/mnt:ro" -w /mnt node:19-alpine npx -y ajv-cli@5.0.0 test -s .ci/.docker-images.schema.json -d .ci/.docker-images.yml --valid --verbose
