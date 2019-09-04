#!/usr/bin/env bash
set -eo pipefail

FILENAME=$1
BASENAME=$(basename "${FILENAME}")
## Further details: https://github.com/elastic/infra/blob/master/flavortown/jjbb/README.md#how-do-i-test-changes-locally

TMPFOLDER=$(mktemp -q -d /tmp/pre-commit.XXXXXX)

function finish {
  rm -rf "${TMPFOLDER}"
}
trap finish EXIT

IMAGE="docker.elastic.co/infra/jjbb"
docker pull "${IMAGE}" > /dev/null

echo 'Transform JJBB to JJB'
docker run -t --rm --user "$(id -u):$(id -g)" \
        -v "${TMPFOLDER}:/tmp" \
        -v "$(pwd):/jjbb" -w /jjbb "${IMAGE}" --write-yaml --yaml-output-dir=/tmp
printf '\tpassed\n'

echo 'Validate JJB'
JJB_REPORT="${TMPFOLDER}/jjb.xml"
set +e
docker run -t --rm --user "$(id -u):$(id -g)" \
        -v "${TMPFOLDER}:/jjbb" \
        -w '/jjbb' \
        -e HOME=/tmp \
        widerplan/jenkins-job-builder -l error test "${BASENAME}" > "${JJB_REPORT}"

# shellcheck disable=SC2181
if [ $? -gt 0 ] ; then
  cat "${JJB_REPORT}"
  exit 1
else
  printf '\tpassed\n'
fi

echo 'Create JJB locally'
set -e
docker run -t --rm --user "$(id -u):$(id -g)" \
        -v "${TMPFOLDER}:/jjbb" \
        -e HOME=/tmp \
        -w '/jjbb' \
        -v "$(pwd)/local/jenkins_jobs.ini":/etc/jenkins_jobs/jenkins_jobs.ini \
        --network local_apm-pipeline-library \
        widerplan/jenkins-job-builder -l error update "${BASENAME}"
printf '\tpassed\n'
