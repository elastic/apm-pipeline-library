#!/usr/bin/env bash

set -euo pipefail

docker_image="kibana-cloud"
if [[ "${SERVERLESS}" == "true" ]]; then
  docker_image="kibana-serverless"
fi

kibana_commit_sha=$(git rev-parse HEAD)
kibana_stack_version="$(jq -r .version package.json)-SNAPSHOT"
docker_tag="${kibana_stack_version}-${kibana_commit_sha}"
docker_reference="${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${docker_image}:${docker_tag}"
{
  echo "kibana-stack-version=${kibana_stack_version}"
  echo "kibana-commit-sha=${kibana_commit_sha}"
  echo "docker-registry=${DOCKER_REGISTRY}"
  echo "docker-namespace=${DOCKER_NAMESPACE}"
  echo "docker-image=${docker_image}"
  echo "docker-tag=${docker_tag}"
  echo "docker-reference=${docker_reference}"
} >> "${GITHUB_OUTPUT}"
