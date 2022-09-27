#!/usr/bin/env bash
set -eu
echo "OUTPUT_DIR=${OUTPUT_DIR}"
echo "OUTPUT_FILE=${OUTPUT_FILE}"
echo "CONFIG_PATH=${CONFIG_PATH}"
echo "DOCKER_IMAGE=${DOCKER_IMAGE}"

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
    -E http.enabled=true > filebeat_docker_id
