#!/usr/bin/env bash
set -e
IMAGE="ghcr.io/tcort/markdown-link-check:3"
docker pull "${IMAGE}" > /dev/null || true

for f in **/*.md
do
    docker run --rm -t -v "${PWD}:/markdown:ro" -u "$(id -u):$(id -g)" "${IMAGE}" "$@" "/markdown/$f"
done
