#!/usr/bin/env bash
#
# Builds a docker image for Kibana inspired by
# https://github.com/elastic/kibana/blob/main/.buildkite/scripts/build_kibana.sh
#
set -euo pipefail

if docker manifest inspect "${DOCKER_REFERENCE}" > /dev/null 2>&1; then
  echo "Docker image ${DOCKER_REFERENCE} already exists. Skipping build."
  exit 0
fi

export BABEL_DISABLE_CACHE=true
export FORCE_COLOR=1
export NODE_OPTIONS=" --max-old-space-size=4096"
export BUILD_TS_REFS_DISABLE="true"

echo "::group::Bootstrap"
time yarn kbn clean
time yarn kbn bootstrap
echo "::endgroup::"

# https://github.com/elastic/kibana/blob/main/.buildkite/scripts/build_kibana.sh#L11-L19
echo "::group::Build Linux package"
export KBN_NP_PLUGINS_BUILT=true
time node scripts/build \
  --skip-os-packages \
  --skip-canvas-shareable-runtime \
  --skip-cdn-assets \
  --skip-docker-contexts
echo "::endgroup::"

# https://github.com/elastic/kibana/blob/main/.buildkite/scripts/build_kibana.sh#L21-L34
echo "::group::Build docker images"
if [ "${SERVERLESS}" == "false" ] ; then
  time node scripts/build \
        --docker-images \
        --docker-namespace="${DOCKER_NAMESPACE}" \
        --docker-tag="${DOCKER_TAG}" \
        --docker-push \
        --skip-archives \
        --skip-initialize \
        --skip-cdn-assets \
        --skip-docker-contexts \
        --skip-docker-ubi \
        --skip-docker-ubuntu \
        --skip-generic-folders \
        --skip-platform-folders \
        --skip-docker-serverless
else
  # enable Docker multiarch support
  docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
  time node scripts/build \
        --release \
        --docker-cross-compile \
        --docker-images \
        --docker-namespace="${DOCKER_NAMESPACE}" \
        --docker-tag="${DOCKER_TAG}" \
        --docker-push \
        --skip-cdn-assets \
        --skip-docker-contexts \
        --skip-docker-ubi \
        --skip-docker-ubuntu \
        --skip-docker-cloud
fi
echo "::endgroup::"
