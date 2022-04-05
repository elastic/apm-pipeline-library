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

@Library('apm@main') _

pipeline {
  agent { label 'ubuntu && immutable' }
  environment {
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
    DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
    DOCKERELASTIC_SECRET = 'secret/apm-team/ci/docker-registry/prod'
    BEATS_MAILING_LIST = "${params.BEATS_MAILING_LIST}"
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
  parameters {
    booleanParam(name: 'DRY_RUN_MODE', defaultValue: false, description: 'If true, allows to execute this pipeline in dry run mode.')
  }
  stages {
    stage('Top failing Tests - last 7 days') {
      steps {
        setEnvVar('YYYY_MM_DD', new Date().format("yyyy-MM-dd", TimeZone.getTimeZone('UTC')))
        generateSteps()
      }
    }
    stage('Sync GitHub labels') {
      steps {
        build(job: 'apm-shared/github-syncup-labels-obs-dev-pipeline',
          parameters: [
            booleanParam(name: 'DRY_RUN_MODE', value: params.DRY_RUN_MODE),
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
            booleanParam(name: 'DRY_RUN_MODE', value: params.DRY_RUN_MODE)
          ],
          propagate: false,
          wait: false
        )
      }
    }
    stage('Stalled Beats Bumps') {
      steps {
        runNotifyStalledBeatsBumps(branches: ['main', '8.<minor>', '8.<next-patch>', '7.<minor>'], to: env.BEATS_MAILING_LIST)
      }
    }
    stage('Stalled Elastic Agent Bumps') {
      steps {
        echo 'TBC'
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def runNotifyStalledBeatsBumps(Map args = [:]) {
  def branches = getBranchesFromAliases(aliases: args.branches)

  def quietPeriod = 0
  branches.each { branch ->
    notifyStalledBeatsBumps(branch: branch,
                            subject: "[${branch}] ${YYYY_MM_DD}: Elastic Stack version has not been updated recently.",
                            sendEmail: params.DRY_RUN_MODE,
                            to: args.to)
  }
}

def runWatcherForBranch(Map args = [:]){
  def branches = getBranchesFromAliases(aliases: args.branches)

  branches.each { branch ->
    try {
      runWatcher(watcher: "report-${args.project}-top-failing-tests-weekly-${branch}",
                 subject: "[${args.project}@${branch}] ${env.YYYY_MM_DD}: Top failing ${args.project} tests in ${branch} branch - last 7 days",
                 sendEmail: params.DRY_RUN_MODE,
                 to: args.to,
                 debugFileName: "${args.project}-${branch}.txt")
    } catch(err) {
      // We don't want to fail but keep sending email for all the branches
      log(level: 'WARN', text: "runWatcher: failed (${err.toString()}).")
    }
  }
}

def generateSteps(Map args = [:]) {
  def projects = readYaml(file: '.ci/.weekly-tests-email.yml')
  projects['projects'].each { project ->
    if (project.get('enabled', true)) {
      runWatcherForBranch(project: project.repo,
                          to: project.email,
                          branches: project.branches)
    } else {
      echo "runWatcherForBranch: ${project.repo} is disabled"
    }
  }
}
