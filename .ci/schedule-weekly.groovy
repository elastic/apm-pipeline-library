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
    stage('Update Labels') {
      agent { label 'linux && immutable' }
      steps {
        updateLabels()
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def updateLabels() {
  git("https://github.com/elastic/observability-dev.git")
  def projects = readYaml(file: '.github/labels-repos.yml')['projects']
  withCredentials([string(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7', variable: 'GITHUB_TOKEN')]) {
    projects?.each { project ->
      updateLabels(project, '.github/labels.yml')
      def projectName = project.replaceAll('.*/', '')
      def specificLabelsFile = ".github/${projectName}-labels.yml"
      if (fileExists(specificLabelsFile)) {
        updateLabels(project, specificLabelsFile)
      }
    }
  }
}

def updateLabels(project, file='.github/labels.yml') {
  withEnv(["PROJECT=${project}", "LABELS_FILE=${file}"]){
    sh '''
      set +x
      docker run --rm -t \
      -v $(pwd):/src \
      -w /src \
      docker.elastic.co/observability-ci/github-label-sync \
        --dry-run \
        --access-token ${GITHUB_TOKEN} \
        ${PROJECT} \
        --labels ${LABELS_FILE} \
        --allow-added-labels
    '''
  }
}
