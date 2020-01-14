#!/usr/bin/env bash
set -eo pipefail
## Further details: https://github.com/elastic/infra/blob/master/flavortown/jjbb/README.md#how-do-i-test-changes-locally

TMPFOLDER=$(mktemp -q -d /tmp/pre-commit.XXXXXX)

function finish {
  rm -rf "${TMPFOLDER}"
}
trap finish EXIT

IMAGE="docker.elastic.co/infra/jjbb"
docker pull "${IMAGE}" > /dev/null || true

echo 'Transform JJBB to JJB'
docker run -t --rm --user "$(id -u):$(id -g)" \
        -v "${TMPFOLDER}:/tmp" \
        -v "$(pwd):/jjbb" -w /jjbb "${IMAGE}" --write-yaml --yaml-output-dir=/tmp

echo 'Validate JJB'
JJB_REPORT="${TMPFOLDER}/jjb.out"
set +e
IMAGE="widerplan/jenkins-job-builder"
docker pull "${IMAGE}" > /dev/null || true
docker run -t --rm --user "$(id -u):$(id -g)" \
        -v "${TMPFOLDER}:/jjbb" \
        -e HOME=/tmp \
        "${IMAGE}" -l error test /jjbb > "${JJB_REPORT}"

# shellcheck disable=SC2181
if [ $? -gt 0 ] ; then
  cat "${JJB_REPORT}"
  exit 1
fi
