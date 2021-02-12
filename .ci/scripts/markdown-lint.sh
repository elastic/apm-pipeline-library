#!/usr/bin/env bash
set -e
IMAGE="ghcr.io/tcort/markdown-link-check:3.8.5"
docker pull "${IMAGE}" > /dev/null || true

for f in **/*.md
do
    docker run --rm -t -v "${PWD}:/markdown:ro" -u "$(id -u):$(id -g)" "${IMAGE}" "--progress" "/markdown/${f}"
done
