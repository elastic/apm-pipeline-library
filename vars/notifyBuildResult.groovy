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
      /*
      emailext body: '''${SCRIPT, template="groovy-html.template"}''',
        mimeType: 'text/html',
        subject: "${currentBuild.currentResult} ${env.JOB_NAME}#${env.BRANCH_NAME} ${env.BUILD_NUMBER}",
        attachLog: true,
        compressLog: true,
        recipientProviders: [brokenTestsSuspects(), brokenBuildSuspects(), upstreamDevelopers()],
        to: "ivan.fernandez@elastic.co"
        */
      catchError {
        generateBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)

        def notificationManager = new co.elastic.NotificationManager()
        notificationManager.notifyEmail(
          build: readJSON(file: "build-info.json"),
          buildStatus: currentBuild.currentResult,
          emailRecipients: ["ivan.fernandez@elastic.co"],
          testsSummary: readJSON(file: "tests-summary.json"),
          changeSet: readJSON(file: "changeSet-info.json"),
          statsUrl: "${es}/app/kibana",
          log: readFile(file: "pipeline-log-summary.txt"),
          testsErrors: readJSON(file: "tests-info.json"),
          stepsErrors: readJSON(file: "steps-info.json")
        )

        def datafile = readFile(file: "build-report.json")
        sendDataToElasticsearch(es: es, secret: secret, data: datafile)

        archiveArtifacts(allowEmptyArchive: true, artifacts: '*.json')
      }
    }
  }
}
