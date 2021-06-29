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

@Library('apm@master') _

// To store all the latest release versions
@Field def latestVersions

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'apm-pipeline-library'
    ORG_NAME = 'elastic'
    HOME = "${env.WORKSPACE}"
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL = 'INFO'
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
        git(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', url: "https://github.com/${ORG_NAME}/${REPO}.git")
      }
    }
    stage('Fetch latest versions') {
      steps {
        script {
          latestVersions = artifactsApi(action: 'latest-release-versions')
        }
        archiveArtifacts 'latest-release-versions.json'
      }
    }
    stage('Send Pull Request'){
      options {
        warnError('Pull Requests failed')
      }
      steps {
        generateSteps()
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
  def projects = readYaml(file: '.ci/.bump-stack-release-version.yml')
  projects['projects'].each { project ->
    matrix( agent: 'linux && immutable',
            axes:[
              axis('REPO', [project.repo]),
              axis('BRANCH', project.branches),
              axis('ENABLED', [project.get('enabled', true)])
            ],
            excludes: [ axis('ENABLED', [ false ]) ]
    ) {
      bumpStackVersion(repo: env.REPO,
                       scriptFile: "${project.script}",
                       branch: env.BRANCH,
                       labels: project.get('labels', ''),
                       title: project.get('title', ''))
    }
  }
}

def bumpStackVersion(Map args = [:]){
  catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
    def arguments = prepareArguments(args)
    createPullRequest(arguments)
  }
}

def prepareArguments(Map args = [:]){
  def repo = args.containsKey('repo') ? args.get('repo') : error('prepareArguments: repo argument is required')
  def scriptFile = args.containsKey('scriptFile') ? args.get('scriptFile') : error('prepareArguments: scriptFile argument is required')
  def branch = args.containsKey('branch') ? args.get('branch') : error('prepareArguments: branch argument is required')
  def labels = args.get('labels', '').replaceAll('\\s','')
  def title = args.get('title', '').trim() ? args.title : '[automation] Update Elastic stack release version'
  log(level: 'INFO', text: "prepareArguments(repo: ${repo}, branch: ${branch}, scriptFile: ${scriptFile}, labels: '${labels}', title: '${title}')")
  def message = createPRDescription(latestVersions)
  if (labels.trim() && !labels.contains('automation')) {
    labels = "automation,${labels}"
  }
  return [repo: repo, branchName: branch, title: "${title} ${latestVersions}", labels: labels, scriptFile: scriptFile, stackVersions: latestVersions, message: message]
}

def createPullRequest(Map args = [:]) {
  prepareContext(repo: args.repo, branchName: args.branchName)
  if (args?.stackVersions?.size() == 0) {
    error('createPullRequest: stackVersions is empty. Review the artifacts-api for the branch ' + args.branchName)
  }
  sh(script: """git checkout -b "update-stack-version-\$(date "+%Y%m%d%H%M%S")-${args.branchName}" """, label: "Git branch creation")
  if(args.stackVersions.size() >= 2){
    sh(script: "${args.scriptFile} '${args.stackVersions[args.stackVersions.size() - 2]}' '${args.stackVersions[args.stackVersions.size() - 1]}'", label: "Prepare changes for ${args.repo}")
  } else if (args.stackVersions.size() == 1){
    sh(script: "${args.scriptFile} '${args.stackVersions[0]}' ''", label: "Prepare changes for ${args.repo}")
  } else {
    error("There is no release versions")
  }

  if (params.DRY_RUN_MODE) {
    log(level: 'INFO', text: "DRY-RUN: createPullRequest(repo: ${args.stackVersions}, labels: ${args.labels}, message: '${args.message}', base: '${args.branchName}', title: '${args.title}')")
    return
  }

  // If a similar PR was already created then do nothing.
  // This should avoid duplicated PRs when they have not been merged yet.
  if (githubPrExists(args)) {
    log(level: 'INFO', text: 'A similar Pull Request already exists.')
    return
  }

  if (anyChangesToBeSubmitted("${args.branchName}")) {
    githubCreatePullRequest(title: "${args.title}", labels: "${args.labels}", description: "${args.message}", base: "${args.branchName}")
  } else {
    log(level: 'INFO', text: "There are no changes to be submitted.")
  }
}

def anyChangesToBeSubmitted(String branch) {
  return sh(returnStatus: true, script: "git diff --quiet HEAD..${branch}") != 0
}

def prepareContext(Map args = [:]) {
  deleteDir()
  setupAPMGitEmail(global: true)
  git(url: "https://github.com/${ORG_NAME}/${args.repo}.git",
      branch: args.branchName,
      credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
}

def createPRDescription(versionEntry) {
  return """### What \n Bump stack version with the latest release. \n ### Further details \n ${versionEntry}"""
}
