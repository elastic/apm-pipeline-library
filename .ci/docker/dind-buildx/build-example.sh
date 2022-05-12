#!/bin/bash
#
# This script show how to use the dind-buildx Docker image to build a Docker image for multiple architectures.

export DOCKER_USER=${DOCKER_USER:?-"Missing environment variable."}
export DOCKER_PASSWORD=${DOCKER_PASSWORD:?-"Missing environment variable."}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-"docker.elastic.co"}

docker run -it \
  -e DOCKER_USER \
  -e DOCKER_PASSWORD \
  -e DOCKER_REGISTRY \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$(pwd):/app" \
  -w /app \
  docker.elastic.co/observability-ci/dind-buildx:20.10.14 """
docker login -u ${DOCKER_USER}
docker buildx ls
echo 'Create builder'
docker buildx create --name mybuilder
docker buildx use mybuilder
docker buildx inspect --bootstrap
echo 'Build Docker image'
docker buildx build --platform linux/amd64,linux/arm64 -t docker.elastic.co/observability-ci/buildx-tests:latest --push .
docker buildx imagetools inspect docker.elastic.co/observability-ci/buildx-tests:latest
#docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 -t buildx-tests:latest --output=type=image .
#docker import buildx-test.tar
"""
