#!/usr/bin/env bash

for i in $(docker images |grep apm-integration-testing-tests-|cut -d " " -f 1)
do
  docker tag "$i" "docker.elastic.co/observability-ci/$i"
  docker push "docker.elastic.co/observability-ci/$i"
done

echo "Docker images"
echo "============="
for i in $(docker images |grep docker.elastic.co/observability-ci/apm-integration-testing-tests-|cut -d " " -f 1)
do
  echo "$i"
done
