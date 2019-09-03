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

import groovy.transform.Field

@Library('apm@current') _

@Field def results = [:]

pipeline {
  agent none
  environment {
    BASE_DIR="src"
    PIPELINE_LOG_LEVEL='INFO'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '2', artifactNumToKeepStr: '2'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    cron 'H H(3-4) * * 1-5'
    issueCommentTrigger('(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'registry', defaultValue: "docker.elastic.co", description: "")
    string(name: 'secret', defaultValue: "secret/apm-team/ci/docker-registry/prod", description: "")
    booleanParam(name: 'beats_runtime_dependencies', defaultValue: "false", description: "If it's needed to build Beats' runtime dependencies")
  }
  stages {
    stage('Build Beats test Docker images'){
      agent { label 'immutable && docker' }
      options {
        skipDefaultCheckout()
        warnError('Build Beats Docker images failed')
      }
      when{
        beforeAgent true
        expression { return params.beats_runtime_dependencies }
      }
      steps {
        checkout scm
        dockerLoginElasticRegistry()
        dir("beats-images"){
          git('https://github.com/elastic/beats.git')
          sh(label: 'Test Docker containers', script: 'echo "make -C .ci/docker all-tests"')
        }
        sh(label: 'Push Docker images', script: 'echo "make -C .ci/docker all-push"')
      }
      post {
        always {
          junit(allowEmptyResults: true,
            keepLongStdio: true,
            testResults: "${BASE_DIR}/**/junit-*.xml")
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

def dockerLoginElasticRegistry(){
  if(params.secret != null && "${params.secret}" != ""){
     dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
  }
}
