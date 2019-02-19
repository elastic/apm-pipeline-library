#!/usr/bin/env bash
#
# Validate a Jenkins file syntax againt a Jenkins instance.
# https://jenkins.io/doc/book/pipeline/development/
#
# curl (REST API)
# Assuming "anonymous read access" has been enabled on your Jenkins instance.
# JENKINS_URL=[root URL of Jenkins master]
# JENKINS_CRUMB is needed if your Jenkins master has CRSF protection enabled as it should

if [ -z "${JENKINS_URL}" ]; then
  echo "JENKINS_URL not defined"
  echo "usage: $0 [Jenkinsfile]"
  exit 1
fi

JENKINS_FILE=${1:-"Jenkinsfile"}

if [ ! -f "${JENKINS_FILE}" ]; then
  echo "${JENKINS_FILE} not found"
  echo "usage: $0 [Jenkinsfile]"
  exit 1
fi

JENKINS_CRUMB=`curl "$JENKINS_URL/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"`
curl -X POST -H "$JENKINS_CRUMB" -F "jenkinsfile=<${JENKINS_FILE}" "$JENKINS_URL/pipeline-model-converter/validate"
