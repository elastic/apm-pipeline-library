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

pipeline {
  agent { label 'ubuntu && immutable' }
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    NOTIFY_TO = credentials('notify-to')
    REPOS = 'elastic/beats,elastic/apm-pipeline-library'
  }
  options {
    timeout(time: 2, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  stages {
    stage('Checkout'){
      steps {
        git(url: 'https://github.com/v1v/issue-crawler', branch: 'observability')
      }
    }
    stage('Build'){
      steps {
        sh 'make docker-build'
      }
    }
    stage('Run'){
      steps {
        prepareCredentials() {
          sh(label: 'Issue Crawler', script: '''
            set +x
            docker run -t --rm \
              -e GITHUB_OAUTH_TOKEN=${GITHUB_OAUTH_TOKEN} \
              -e ES_HOST=${ES_HOST} \
              -e ES_AUTH=${ES_AUTH} \
              -e REPOS=${REPOS} \
              issue-crawler:latest
          ''')
        }
      }
    }
  }
}


def prepareCredentials(body) {
  def props = getVaultSecret(secret: 'secret/observability-team/ci/jenkins-stats-cloud')
  if(props?.errors){
    error "prepareES: Unable to get credentials from the vault: " + props.errors.toString()
  }
  def value = props?.data
  def user = value?.user
  def password = value?.password
  def url = value?.url
  if(url == null || user == null || password == null){
    error "prepareES: was not possible to get authentication info to send data."
  }

  withEnvMask(vars: [
    [var: "ES_HOST", password: url ],
    [var: "ES_AUTH", password: "${user}:${password}" ],
  ]){
    withCredentials([string(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7',
                            variable: 'GITHUB_OAUTH_TOKEN')]) {
      body()
    }
  }
}
