// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**

Send an email message with a summary of the build result,
and send some data to Elastic search.

notifyBuildResult(es: 'http://elastisearch.example.com:9200', secret: 'secret/team/ci/elasticsearch')

**/

import co.elastic.NotificationManager

def call(Map args = [:]) {
  def rebuild = args.containsKey('rebuild') ? args.rebuild : true
  node('master || metal || immutable'){
    stage('Reporting build status'){
      def secret = args.containsKey('secret') ? args.secret : 'secret/apm-team/ci/jenkins-stats-cloud'
      def es = args.containsKey('es') ? args.es : getVaultSecret(secret: secret)?.data.url
      def to = args.containsKey('to') ? args.to : [ customisedEmail(env.NOTIFY_TO)]
      def statsURL = args.containsKey('statsURL') ? args.statsURL : "ela.st/observabtl-ci-stats"
      def shouldNotify = args.containsKey('shouldNotify') ? args.shouldNotify : !env.CHANGE_ID && currentBuild.currentResult != "SUCCESS"

      catchError(message: "Let's unstable the stage and stable the build.", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
        archiveArtifacts(allowEmptyArchive: true, artifacts: '*.json')

        if(shouldNotify){
          log(level: 'DEBUG', text: "notifyBuildResult: Notifying results by email.")
          def notificationManager = new NotificationManager()
          notificationManager.notifyEmail(
            build: readJSON(file: "build-info.json"),
            buildStatus: currentBuild.currentResult,
            emailRecipients: to,
            testsSummary: readJSON(file: "tests-summary.json"),
            changeSet: readJSON(file: "changeSet-info.json"),
            statsUrl: "${statsURL}",
            log: readFile(file: "pipeline-log-summary.txt"),
            testsErrors: readJSON(file: "tests-info.json"),
            stepsErrors: readJSON(file: "steps-info.json")
          )
        }

        def datafile = readFile(file: "build-report.json")
        sendDataToElasticsearch(es: es, secret: secret, data: datafile)
      }
    }
  }

  if (rebuild) {
    log(level: 'DEBUG', text: 'notifyBuildResult: rebuild is enabled.')
    // If there is an issue with the default checkout then the env variable
    // won't be created and let's rebuild
    if (isGitCheckoutIssue()) {
      currentBuild.description = "Issue: timeout checkout ${currentBuild.description?.trim() ? currentBuild.description : ''}"
      rebuildPipeline()
    } else {
      log(level: 'DEBUG', text: "notifyBuildResult: either it was not a failure or GIT_BUILD_CAUSE='${env.GIT_BUILD_CAUSE?.trim()}'.")
    }
  }
}

def customisedEmail(String email) {
  if (email) {
    // default name should be the REPO env variable.
    def suffix = env.REPO

    // If JOB_NAME then let's get its parent folder name
    if (env.JOB_NAME) {
      def folders = env.JOB_NAME.split("/")
      if (folders?.length > 0) {
        suffix = folders[0]
      }
    }
    if (suffix?.trim()) {
      return email.replace('@', "+${suffix}@")
    } else {
      return email
    }
  }
  return ''
}

def isGitCheckoutIssue() {
  return currentBuild.currentResult == 'FAILURE' && !env.GIT_BUILD_CAUSE?.trim()
}
