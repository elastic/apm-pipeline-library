#!/usr/bin/env bash

REGISTRY="docker.elastic.co/observability-ci"
IMAGES_PREFIX="apm-integration-testing-tests-"

for i in $(docker images |grep "${IMAGES_PREFIX}"|cut -d " " -f 1)
do
  docker tag "$i" "${REGISTRY}/$i"
  docker push "${REGISTRY}/$i"
done

echo "Docker images"
echo "============="
for i in $(docker images |grep "${REGISTRY}/${IMAGES_PREFIX}"|cut -d " " -f 1)
do
  echo "$i"
done
