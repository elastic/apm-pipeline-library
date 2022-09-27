#!/usr/bin/env bash
set -eu

echo "OUTPUT_DIR=${OUTPUT_DIR}"
echo "OUTPUT_FILE=${OUTPUT_FILE}"
echo "CONFIG_PATH=${CONFIG_PATH}"
echo "DOCKER_IMAGE=${DOCKER_IMAGE}"

docker run \
  --detach \
  -v "${OUTPUT_DIR}:/output" \
  -v "${CONFIG_PATH}:/usr/share/metricbeat/metricbeat.yml" \
  -v /var/lib/docker/containers:/var/lib/docker/containers \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -u 0:0 \
  --mount type=bind,source=/proc,target=/hostfs/proc,readonly \
  --mount type=bind,source=/sys/fs/cgroup,target=/hostfs/sys/fs/cgroup,readonly \
  --mount type=bind,source=/,target=/hostfs,readonly \
  -e OUTPUT_FILE="${OUTPUT_FILE}" \
  --net=host \
  "${DOCKER_IMAGE}" \
    --strict.perms=false \
    -environment container \
    -E http.enabled=true \
    -e -system.hostfs=/hostfs > docker_id
