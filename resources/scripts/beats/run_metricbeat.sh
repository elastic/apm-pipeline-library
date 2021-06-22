#!/usr/bin/env bash
set -eu

echo "CONFIG_PATH=${CONFIG_PATH}"
echo "DOCKER_IMAGE=${DOCKER_IMAGE}"

docker run \
  --detach \
  -v "${CONFIG_PATH}:/usr/share/metricbeat/metricbeat.yml" \
  -u 0:0 \
  --mount type=bind,source=/proc,target=/hostfs/proc,readonly \
  --mount type=bind,source=/sys/fs/cgroup,target=/hostfs/sys/fs/cgroup,readonly \
  --mount type=bind,source=/,target=/hostfs,readonly \
  --net=host \
  -e ES_URL="${ES_URL}" \
  -e ES_USERNAME="${ES_USERNAME}" \
  -e ES_PASSWORD="${ES_PASSWORD}" \
  "${DOCKER_IMAGE}" \
    --strict.perms=false \
    -environment container \
    -E http.enabled=true \
    -e -system.hostfs=/hostfs > docker_id
