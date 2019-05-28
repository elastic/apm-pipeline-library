#!/usr/bin/env bash
# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
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
