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

@Library('apm@current') _

pipeline {
  agent { label 'docker' }
  environment {
    BASE_DIR="src"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    REPO = "git@github.com:elastic/apm-pipeline-library.git"
    PIPELINE_LOG_LEVEL='INFO'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    issueCommentTrigger('(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'registry', defaultValue: "docker.elastic.co", description: "")
    string(name: 'tag_prefix', defaultValue: "observability-ci", description: "")
    string(name: 'version', defaultValue: "daily", description: "")
    string(name: 'elastic_stack', defaultValue: "8.0.0-SNAPSHOT", description: "")
    string(name: 'secret', defaultValue: "secret/apm-team/ci/docker-registry/prod", description: "")
    string(name: 'branch_specifier', defaultValue: "master", description: "")
  }
  stages {
    stage('Checkout') {
      steps {
        deleteDir()
        gitCheckout(basedir: ".",
          branch: "${params.branch_specifier}",
          repo: "${REPO}",
          credentialsId: "${JOB_GIT_CREDENTIALS}"
        )
      }
    }
    stage('Get Docker images'){
      steps {
        script {
          if(params.secret != null && "${params.secret}" != ""){
             dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
          }
          sh(label: 'Get Docker images', script: "./resources/scripts/getDockerImages.sh ${params.elastic_stack}")
        }
      }
    }
    stage('Push Docker images'){
      steps {
        script {
          if(params.secret != null && "${params.secret}" != ""){
             dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
          }
          sh(label: 'Push Docker images', script: "./resources/scripts/pushDockerImages.sh ${params.elastic_stack} ${params.tag_prefix} ${params.version} ${params.registry}")
        }
      }
    }
  }
  post {
    always {
      notifyBuildResult()
    }
  }
}
