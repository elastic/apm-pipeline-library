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

@Library('apm@master') _

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
    stage('Top failing Beats tests - last 7 days') {
      steps {
        setEnvVar('YYYY_MM_DD', new Date().format("yyyy-MM-dd", TimeZone.getTimeZone('UTC')))
        runWatcher(watcher: 'report-beats-top-failing-tests-weekly-master', subject: "[master] ${env.YYYY_MM_DD}: Top failing Beats tests - last 7 days", sendEmail: true, to: 'beats-contrib@elastic.co')
        runWatcher(watcher: 'report-beats-top-failing-tests-weekly-7.x', subject: "[7.x] ${env.YYYY_MM_DD}: Top failing Beats tests - last 7 days", sendEmail: true, to: 'beats-contrib@elastic.co')
        runWatcher(watcher: 'report-beats-top-failing-tests-weekly-7-release', subject: "[7-release] ${env.YYYY_MM_DD}: Top failing Beats tests - last 7 days", sendEmail: true, to: 'beats-contrib@elastic.co')
      }
    }
    stage('Sync GitHub labels') {
      steps {
        build(job: 'apm-shared/github-syncup-labels-obs-dev-pipeline',
          parameters: [
            booleanParam(name: 'DRY_RUN_MODE', value: false),
          ],
          propagate: false,
          wait: false
        )
      }
    }
    stage('Run Tasks'){
      steps {
        build(job: 'apm-shared/apm-test-pipeline-mbp/master',
          parameters: [
            booleanParam(name: 'Run_As_Master_Branch', value: true),
          ],
          propagate: false,
          wait: false
        )
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}
