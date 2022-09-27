#!/usr/bin/env bash

ID=${1:?"Missing parameter"}
URL=${2:-"http://localhost:5066/stats?pretty"}

echo "INFO: print existing docker context"
docker ps -a || true

echo "DEBUG: print docker inspect"
docker inspect "${ID}"

echo "DEBUG: print existing docker logs"
docker logs -f "${ID}"

echo "INFO: wait for the docker container to be available"
N=0
until docker exec "${ID}" curl -sSfI --retry 10 --retry-delay 5 --max-time 5 "${URL}"
do
  sleep 5
  if [ "${N}" -gt 6 ]; then
    break;
  fi
  N=$(("${N}" + 1))
done
