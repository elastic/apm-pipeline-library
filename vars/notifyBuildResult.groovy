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
import co.elastic.TimeoutIssuesCause
import hudson.tasks.test.AbstractTestResultAction
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper

def call(Map args = [:]) {
  def rebuild = args.containsKey('rebuild') ? args.rebuild : true
  def downstreamJobs = args.containsKey('downstreamJobs') ? args.downstreamJobs : [:]
  def notifyPRComment = args.containsKey('prComment') ? args.prComment : true
  node('master || metal || immutable'){
    stage('Reporting build status'){
      def secret = args.containsKey('secret') ? args.secret : 'secret/observability-team/ci/jenkins-stats-cloud'
      def es = args.containsKey('es') ? args.es : getVaultSecret(secret: secret)?.data.url
      def to = args.containsKey('to') ? args.to : [ customisedEmail(env.NOTIFY_TO)]
      def statsURL = args.containsKey('statsURL') ? args.statsURL : "https://ela.st/observabtl-ci-stats"
      def shouldNotify = args.containsKey('shouldNotify') ? args.shouldNotify : !isPR() && currentBuild.currentResult != "SUCCESS"

      catchError(message: 'There were some failures with the notifications', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        def data = getBuildInfoJsonFiles(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER, returnData: true)
        data['docsUrl'] =  "http://${env?.REPO_NAME}_${env?.CHANGE_ID}.docs-preview.app.elstc.co/diff"
        data['emailRecipients'] = to
        data['statsUrl'] = statsURL
        def notificationManager = new NotificationManager()
        if(shouldNotify){
          log(level: 'DEBUG', text: 'notifyBuildResult: Notifying results by email.')
          notificationManager.notifyEmail(data)
        }
        // Should notify if it is a PR and it's enabled
        if(notifyPRComment && isPR()) {
          log(level: 'DEBUG', text: "notifyBuildResult: Notifying results in the PR.")
          notificationManager.notifyPR(data)
        }
        log(level: 'DEBUG', text: 'notifyBuildResult: Generate build report.')
        notificationManager.generateBuildReport(data)
      }

      catchError(message: 'There were some failures when sending data to elasticsearch', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        timeout(5) {
          def datafile = readFile(file: 'build-report.json')
          sendDataToElasticsearch(es: es, secret: secret, data: datafile)
        }
      }

      catchError(message: 'There were some failures when cleaning up the workspace ', buildResult: 'SUCCESS') {
        deleteDir()
      }
    }
  }

  if (rebuild) {
    log(level: 'DEBUG', text: 'notifyBuildResult: rebuild is enabled.')

    // Supported scenarios to rebuild in case of a timeout issue:
    // 1) If there is an issue in the upstream with the default checkout then the env variable
    // won't be created.
    // 2) If there is an issue with any of the dowstreamjobs related to the timeout.
    if (isGitCheckoutIssue()) {
      currentBuild.description = "Issue: timeout checkout ${currentBuild.description?.trim() ? currentBuild.description : ''}"
      rebuildPipeline()
    } else if (isAnyDownstreamJobFailedWithTimeout(downstreamJobs)) {  // description is handled with the analyseDownstreamJobsFailures method
      rebuildPipeline()
    } else {
      log(level: 'DEBUG', text: "notifyBuildResult: either it was not a failure or GIT_BUILD_CAUSE='${env.GIT_BUILD_CAUSE?.trim()}'.")
    }
  }

  // This is the one in charge to notify the parenstream with the likelihood downstream issues, if any
  analyseDownstreamJobsFailures(downstreamJobs)
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

def analyseDownstreamJobsFailures(downstreamJobs) {
  if (downstreamJobs.isEmpty()) {
    log(level: 'DEBUG', text: 'notifyBuildResult: there are no downstream jobs to be analysed')
  } else {
    def description = []

    // Get all the downstreamJobs that got a TimeoutIssueCause
    downstreamJobs.findAll { k, v -> v instanceof FlowInterruptedException &&
                                     v.getCauses().find { it -> it instanceof TimeoutIssuesCause } }
                  .collectEntries { name, v ->
                    [(name): v.getCauses().find { it -> it instanceof TimeoutIssuesCause }.getShortDescription()]
                  }
                  .each { jobName, issue ->
                    description << issue
                  }

    // Explicitly identify the test cause issues that got failed test cases.
    downstreamJobs.findAll { k, v -> v instanceof RunWrapper && v.resultIsWorseOrEqualTo('UNSTABLE') }
                  .each { k, v ->
                    def testResultAction = v.getRawBuild().getAction(AbstractTestResultAction.class)
                    if (testResultAction != null && testResultAction.getFailCount() > 0 ) {
                      description << "${k}#${v.getNumber()} got ${testResultAction.failCount} test failure(s)"
                    }
                  }
    currentBuild.description = "${currentBuild.description?.trim() ? currentBuild.description : ''} ${description.join('\n')}"
    log(level: 'DEBUG', text: "notifyBuildResult: analyseDownstreamJobsFailures just updated the description with '${currentBuild.description?.trim()}'.")
  }
}

def isAnyDownstreamJobFailedWithTimeout(downstreamJobs) {
  return downstreamJobs?.any { k, v -> v instanceof FlowInterruptedException &&
                                       v.getCauses().find { it -> it instanceof TimeoutIssuesCause } }
}
