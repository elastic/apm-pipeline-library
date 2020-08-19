#!/usr/bin/env bash
set -eo pipefail

## Further details: https://github.com/elastic/infra/blob/master/flavortown/jjbb/README.md#how-do-i-test-changes-locally

JJB_IMAGE="osmman/jenkins-job-builder:3.1.0"
TMPFOLDER=$(mktemp -q -d /tmp/pre-commit.XXXXXX)
LOG_LEVEL="error"

while getopts "l:j:" options; do
  case "${options}" in
    l)
      echo "Setting log level to ${OPTARG}"
      LOG_LEVEL=${OPTARG} ;;
    j)
      echo "Setting job to ${OPTARG}"
      FILENAME=${OPTARG} ;;
    *)
      echo "Error: -l expected argument info|warn|error|debug or needs -f <filename>" ;;
  esac
  done

if [ -z "${FILENAME}" ] ; then
  echo "To specify a job to deploy, you must use -j"
  exit 1
fi

if (($# == 0))
then
  echo "No positional arguments specified. Did you mean -j <filename>?"
fi


BASENAME=$(basename "${FILENAME}")

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
        ${JJB_IMAGE} -l "${LOG_LEVEL}" test "${BASENAME}" > "${JJB_REPORT}"

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
        ${JJB_IMAGE} -l "${LOG_LEVEL}" update "${BASENAME}"
printf '\tpassed\n'
