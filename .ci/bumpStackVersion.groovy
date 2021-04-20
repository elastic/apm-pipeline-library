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

@Field def versions

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'observability-dev'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    HOME = "${env.WORKSPACE}"
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
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
  parameters {
    booleanParam(name: 'DRY_RUN_MODE', defaultValue: false, description: 'If true, allows to execute this pipeline in dry run mode, without sending a PR.')
  }
  stages {
    stage('Checkout') {
      steps {
        dir("${BASE_DIR}"){
          git(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', url: "https://github.com/elastic/${REPO}.git")
        }
      }
    }
    stage('Fetch latest versions') {
      steps {
        script {
          versions = artifactsApi(action: 'latest-versions')
        }
      }
    }
    stage('Send Pull Request'){
      options {
        warnError('Pull Requests failed')
      }
      steps {
        deleteDir()
        unstash 'source'
        dir("${BASE_DIR}"){
          generateSteps()
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

def generateSteps(Map args = [:]) {
  def projects = readYaml(file: '.ci/.bump-stack-version.yml')
  def parallelTasks = [:]
  env.PR_DESCRIPTION = createPRDescription()
  projects['projects'].each { p ->
    p.branches?.each { b ->
      parallelTasks["${p.repo}-${b}"] = generateStep(repo: "${p.repo}",
                                                     scriptFile: "${p.script}",
                                                     branch: b)
    }
  }
  parallel(parallelTasks)
}

def generateStep(Map args = [:]){
  def repo = args.containsKey('repo') ? args.get('repo') : error('generateStep: repo argument is required')
  def scriptFile = args.containsKey('scriptFile') ? args.get('scriptFile') : error('generateStep: scriptFile argument is required')
  def branch = args.containsKey('branch') ? args.get('branch') : error('generateStep: branch argument is required')

  // special macro to look for the latest minor version
  if (branch.contains('<minor>')) {
    def parts = branch.split('.')
    def major = parts[0]
    branch = versions.sort()*.key.find { it.startsWith("${major}.") }
  }
  def versionEntry = versions.get(branch)

  def message = """
  ### What
  Bump stack version with the latest one.
  ### Further details
  ```
  ${versionEntry.version}
  ```
  """
  return {
    node('linux && immutable') {
      catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        deleteDir()
        dir("${BASE_DIR}"){
          setupAPMGitEmail(global: true)
          git(url: "https://github.com/elastic/${repo}.git", branch: branch, credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
          sh(script: "${scriptFile} '${versionEntry.build_id}'", label: "Prepare changes for ${repo}")
          if (params.DRY_RUN_MODE) {
            echo "DRY-RUN: ${repo} with description: '${message}'"
          } else {
            githubCreatePullRequest(title: "bump: stack version '${versionEntry.build_id}'", labels: 'automation', description: "${message}")
          }
        }
      }
    }
  }
}
