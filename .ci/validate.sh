#!/usr/bin/env bash
set +e

if [ -z ${JENKINS_URL} ] ; then
  JENKINS_URL=http://0.0.0.0:18080
else
  CRUMB=$(curl --silent ${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat\(//crumbRequestField,%22:%22,//crumb\))
  FLAG="-H \"${CRUMB}\""
fi

for file in "$@"; do
  curl --silent -X POST ${FLAG} -F "jenkinsfile=<${file}" ${JENKINS_URL}/pipeline-model-converter/validate | grep -i -v successfully
  if [ $? -eq 0 ] ; then
    echo "ERROR: jenkinslint failed for the file '${file}'"
    exit_status=1
  fi
done

exit $exit_status
