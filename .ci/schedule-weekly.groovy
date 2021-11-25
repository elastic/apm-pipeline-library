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
  agent { label 'ubuntu && immutable' }
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
        runWatcherForBranch(branch: 'master')
        runWatcherForBranch(branch: '7.<next>')
        runWatcherForBranch(branch: '8.<current>')
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
    stage('Bump Go release') {
      steps {
        build(job: 'apm-shared/bump-go-release-version-pipeline',
          parameters: [
            booleanParam(name: 'DRY_RUN_MODE', value: false)
          ],
          propagate: false,
          wait: false
        )
      }
    }
    stage('Stalled Beats Bumps') {
      steps {
        notifyStalledBeatsBumps(branch: 'master', sendEmail: false, to: 'beats-contrib@elastic.co')
        notifyStalledBeatsBumps(branch: '8.0', sendEmail: false, to: 'beats-contrib@elastic.co')
        notifyStalledBeatsBumps(branch: '7.16', sendEmail: false, to: 'beats-contrib@elastic.co')
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def runWatcherForBranch(Map args = [:]){
  def branch = args.branch
  if (branch.contains('8.<current>')) {
    current8 = bumpUtils.getCurrentMinorReleaseFor8()
    def parts = current8.split('\\.')
    branch = "${parts[0]}.${parts[1]}"
  }
  if (branch.contains('7.<current>')) {
    current7 = bumpUtils.getCurrentMinorReleaseFor7()
    def parts = current7.split('\\.')
    branch = "${parts[0]}.${parts[1]}"
  }
  if (branch.contains('7.<next>')) {
    current7 = bumpUtils.getNextMinorReleaseFor7()
    def parts = current7.split('\\.')
    branch = "${parts[0]}.${parts[1]}"
  }
  runWatcher(watcher: "report-beats-top-failing-tests-weekly-${branch}",
             subject: "[${branch}] ${env.YYYY_MM_DD}: Top failing Beats tests in ${branch} branch - last 7 days",
             sendEmail: true,
             to: 'beats-contrib@elastic.co')
}
