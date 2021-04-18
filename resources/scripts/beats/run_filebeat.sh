#!/usr/bin/env bash
set -eu

docker run \
  --detach \
  -v "${OUTPUT_DIR}:/output" \
  -v "${CONFIG_PATH}:/usr/share/filebeat/filebeat.yml" \
  -u 0:0 \
  -v /var/lib/docker/containers:/var/lib/docker/containers \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e OUTPUT_FILE="${OUTPUT_FILE}" \
  "${DOCKER_IMAGE}" \
    --strict.perms=false \
    -environment container \
    -E http.enabled=true > docker_id
