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
  agent { label 'master' }
  environment {
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
    DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
    DOCKERELASTIC_SECRET = 'secret/apm-team/ci/docker-registry/prod'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    cron('H H(1-4) * * 1')
  }
  stages {
    stage('Run Tasks'){
      stages {
        stage('Update snapshots') {
          parallel {
            stage('8.0.0-SNAPSHOT'){
              steps {
                grabDockerImages('8.0.0-SNAPSHOT','8.0-SNAPSHOT')
              }
            }
            stage('7.5.0-SNAPSHOT'){
              steps {
                grabDockerImages('7.5.0-SNAPSHOT','7.5.0-SNAPSHOT')
              }
            }
            stage('7.4.0'){
              steps {
                grabDockerImages('7.4.0','7.4')
              }
            }
          }
        }
        stage('Update k8s Clusters'){
          steps {
            updateCluster('8.x.x-SNAPSHOT')
            updateCluster('7.x.x-SNAPSHOT')
            updateCluster('7.x.x')
          }
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

def grabDockerImages(versionToStore, versionToGet){
  build(job: 'apm-shared/apm-docker-es-pipeline',
    parameters: [
      string(name: 'registry', value: 'docker.elastic.co'),
      string(name: 'tag_prefix', value: 'observability-ci'),
      string(name: 'version', value: versionToStore),
      string(name: 'elastic_stack', value: versionToGet),
      string(name: 'secret', value: "${DOCKERELASTIC_SECRET}"),
      string(name: 'branch_specifier', value: 'master')
    ],
    propagate: false,
    wait: true
  )
}

def updateCluster(branch){
  build(job: "apm-shared/observability-test-environments-update-mbp/${branch}",
    parameters: [
      booleanParam(name: 'stop_services', value: true),
      booleanParam(name: 'start_services', value: true)
    ],
    quietPeriod: 10,
    propagate: false,
    wait: false
  )
}
