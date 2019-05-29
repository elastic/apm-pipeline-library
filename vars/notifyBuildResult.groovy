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

def call(Map params = [:]) {
  def es = params.containsKey('es') ? getLogLevelNum(params.es) : 'https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243'
  def secret = params.containsKey('secret') ? params.secret : 'secret/apm-team/ci/java-agent-benchmark-cloud'

  node('master'){
    stage('Reporting build status'){
      emailext body: '''${SCRIPT, template="groovy-html.template"}''',
        mimeType: 'text/html',
        subject: "${currentBuild.currentResult} ${env.JOB_NAME}#${env.BRANCH_NAME} ${env.BUILD_NUMBER}",
        attachLog: true,
        compressLog: true,
        recipientProviders: [brokenTestsSuspects(), brokenBuildSuspects(), upstreamDevelopers()],
        to: "ivan.fernandez@elastic.co"

      generateBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)

      def notificationManager = new co.elastic.NotificationManager()
      def build = readJSON(file: "build-info.json")
      def testsSummary = readJSON(file: "tests-summary.json")
      def testsErrors = null //readJSON(file: "tests-errors.json")
      def changeSet = readJSON(file: "changeSet-info.json")
      def stepsErrors = null //readJSON(file: "steps-errors.json")
      def statsUrl = "${es}/app/kibana"
      def log = readFile(file: "pipeline-log-summary.txt")
      notificationManager.notifyEmail(
        build,
        currentBuild.currentResult,
        ["ivan.fernandez@elastic.co"],
        testsSummary,
        changeSet,
        statsUrl,
        log)

      def datafile = readFile(file: "build-report.json")
      sendReportToElasticsearch(es, secret, datafile)

      archiveArtifacts(allowEmptyArchive: true, artifacts: '*.json')
    }
  }
}

/**
  Grab build related info from the Blueocean REST API and store it on JSON files.
  Then put all togeder in a simple JSON file.
*/
def generateBuildInfoJsonFiles(jobURL, buildNumber){
  def restURLJob = "${jobURL}" - "${env.JENKINS_URL}job/"
  restURLJob = restURLJob.replace("/job/","/")
  restURLJob = "${env.JENKINS_URL}/blue/rest/organizations/jenkins/pipelines/${restURLJob}"
  def restURLBuild = "${restURLJob}/runs/${buildNumber}"

  sh(label: "Get Build info", script: """
    curl -sSL -o job-info.json ${restURLJob}
    curl -sSL -o build-info.json ${restURLBuild}
    curl -sSL -o tests-summary.json ${restURLBuild}/blueTestSummary
    curl -sSL -o tests-info.json ${restURLBuild}/tests
    curl -sSL -o changeSet-info.json ${restURLBuild}/changeSet
    curl -sSL -o artifacts-info.json ${restURLBuild}/artifacts
    curl -sSL -o steps-info.json ${restURLBuild}/steps
    curl -sSL -o pipeline-log.txt ${restURLBuild}/log
    """)

  sh(label: "Console lg sumary", script: "tail -n 100 pipeline-log.txt > pipeline-log-summary.txt")
  //sh(label: "Get Tests failed", script: "cat tests-info.json|jq '.[]|select(.status==\"FAILED\")|[{name,hasStdLog,errorStackTrace,errorDetails,duration]}' > tests-errors.json")
  //sh(label: "Get steps failed", script: "cat steps-info.json|jq '.[]|select(.result==\"FAILURE\")|[{displayName,displayDescription,durationInMillis,id,result,state,type,startTime,actions}]' > steps-errors.json")


  def json = [:]
  json.job = readJSON(file: "job-info.json")
  json.build = readJSON(file: "build-info.json")
  json.test_summary = readJSON(file: "tests-summary.json")
  json.test = readJSON(file: "tests-info.json")
  json.changeSet = readJSON(file: "changeSet-info.json")
  json.artifacts = readJSON(file: "artifacts-info.json")
  json.steps = readJSON(file: "steps-info.json")
  json.log = readFile(file: "pipeline-log.txt")

  json.build.result = currentBuild.currentResult
  json.build.state = "FINISHED"
  json.build.durationInMillis = json.build.estimatedDurationInMillis

  writeJSON(file: "build-report.json" , json: toJSON(json), pretty: 2)
}

/**
  Send the JSON report file to Elastisearch.
*/
def sendReportToElasticsearch(es, secret, data){
  def props = getVaultSecret(secret: secret)
  if(props?.errors){
     error "notifyBuildResult: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def value = props?.data
  def user = value?.user
  def password = value?.password
  if(data == null || user == null || password == null){
    error "notifyBuildResult: was not possible to get authentication info to send data"
  }

  log(level: 'INFO', text: "notifyBuildResult: sending data...")

  def messageBase64UrlPad = base64encode(text: "${user}:${password}", encoding: "UTF-8")
  httpRequest(url: "${es}/jenkins-builds/_doc/", method: "POST",
      headers: [
          "Content-Type": "application/json",
          "Authorization": "Basic ${messageBase64UrlPad}"],
      data: data.toString() + "\n")
}
