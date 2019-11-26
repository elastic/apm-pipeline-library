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
  agent { label 'linux && immutable' }
  environment {
    REPO = 'beats'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    DOCKER_REGISTRY = 'docker.elastic.co'
    DOCKER_REGISTRY_SECRET = 'secret/apm-team/ci/docker-registry/prod'
    GO_VERSION = "${params.GO_VERSION.trim()}"
    GOPATH = "${env.WORKSPACE}"
    HOME = "${env.WORKSPACE}"
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
    PYTHON_EXE='python2.7'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    rateLimitBuilds(throttle: [count: 60, durationName: 'hour', userBoost: true])
    quietPeriod(10)
  }
  triggers {
    cron '@daily'
  }
  parameters {
    string(name: 'GO_VERSION', defaultValue: '1.12.7', description: "Go version to use.")
    booleanParam(name: "RELEASE_TEST_IMAGES", defaultValue: "true", description: "If it's needed to build & push Beats' test images")
  }
  stages {
    stage('Checkout') {
      steps {
        dir("${BASE_DIR}"){
          git("https://github.com/elastic/${REPO}.git")
        }
      }
    }
    stage('Install dependencies') {
      when {
        expression { return params.RELEASE_TEST_IMAGES }
      }
      steps {
        sh(label: 'Install virtualenv', script: 'pip install --user virtualenv')
      }
    }
    stage('Release Beats Test Docker images'){
      options {
        warnError('Release Beats Docker images failed')
      }
      when {
        expression { return params.RELEASE_TEST_IMAGES }
      }
      steps {
        dockerLogin(secret: "${env.DOCKER_REGISTRY_SECRET}", registry: "${env.DOCKER_REGISTRY}")

        dir("${BASE_DIR}/metricbeat"){
          sh(label: 'Define Python Env', script: 'make python-env')
          // TODO: we are building just MySQL, which is the only one ready
          sh(label: 'Release Docker Images', script: ".ci/script/release-metricbeat-module.sh 'mysql' '${env.GO_VERSION}'")
        }
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}
