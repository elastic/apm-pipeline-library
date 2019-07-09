#!/usr/bin/env bash
set +e

JENKINS_URL=http://0.0.0.0:18080

for file in "$@"; do
  curl --silent -X POST -F "jenkinsfile=<${file}" ${JENKINS_URL}/pipeline-model-converter/validate | grep -i -v successfully
  if [ $? -eq 0 ] ; then
    echo "ERROR: jenkinslint failed for the file '${file}'"
    exit_status=1
  fi
done

exit $exit_status
