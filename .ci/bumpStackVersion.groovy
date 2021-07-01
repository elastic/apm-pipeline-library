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

// To store all the latest snapshot versions
@Field def latestVersions

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'observability-dev'
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
          latestVersions = artifactsApi(action: 'latest-versions')
        }
        archiveArtifacts 'latest-versions.json'
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
  def projects = readYaml(file: '.ci/.bump-stack-version.yml')
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
                       reusePullRequest: project.get('reusePullRequest', false),
                       labels: project.get('labels', ''),
                       assign: project.get('assign', ''),
                       reviewer: project.get('reviewer', ''))
    }
  }
}

def bumpStackVersion(Map args = [:]){
  catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
    def arguments = prepareArguments(args)
    // Let's reuse the existing PullRequest if any.
    if(reusePullRequest(arguments)) {
      return
    }
    createPullRequest(arguments)
  }
}

def prepareArguments(Map args = [:]){
  def repo = args.containsKey('repo') ? args.get('repo') : error('prepareArguments: repo argument is required')
  def scriptFile = args.containsKey('scriptFile') ? args.get('scriptFile') : error('prepareArguments: scriptFile argument is required')
  def branch = args.containsKey('branch') ? args.get('branch') : error('prepareArguments: branch argument is required')
  def reusePullRequest = args.get('reusePullRequest', false)
  def labels = args.get('labels', '').replaceAll('\\s','')
  def assign = args.get('assign', '')
  def reviewer = args.get('reviewer', '')
  log(level: 'INFO', text: "prepareArguments(repo: ${repo}, branch: ${branch}, scriptFile: ${scriptFile}, reusePullRequest: ${reusePullRequest}, labels: '${labels}')")

  def title = '[automation] update elastic stack version for testing'
  def branchName = findBranchName(branch: branch, versions: latestVersions)
  def versionEntry = latestVersions.get(branchName)
  def message = createPRDescription(versionEntry)
  def stackVersion = versionEntry?.build_id
  if (labels.trim() && !labels.contains('automation')) {
    labels = "automation,${labels}"
  }
  return [reusePullRequest: reusePullRequest, repo: repo, branchName: branchName, title: "${title} ${stackVersion}", labels: labels,
          scriptFile: scriptFile, stackVersion: stackVersion, message: message, assign: assign, reviewer: reviewer]
}

def reusePullRequest(Map args = [:]) {
  prepareContext(repo: args.repo, branchName: args.branchName)
  if (args.reusePullRequest && reusePullRequestIfPossible(args)) {
    try {
      sh(script: "${args.scriptFile} '${args.stackVersion}' 'false'", label: "Prepare changes for ${args.repo}")
      if (params.DRY_RUN_MODE) {
        log(level: 'INFO', text: "DRY-RUN: reusePullRequest(repo: ${args.stackVersion}, labels: ${args.labels}, message: '${args.message}', title: '${args.title}', assign: '${assign}', reviewer: '${reviewer}')")
        return true
      }
      withEnv(["REPO_NAME=${args.repo}"]){
        gitPush()
      }
      return true
    } catch(err) {
      log(level: 'INFO', text: "Could not reuse an existing GitHub Pull Request. So fallback to create a new one instead. ${err.toString()}")
    }
  }
  return false
}

def createPullRequest(Map args = [:]) {
  prepareContext(repo: args.repo, branchName: args.branchName)
  if (!args?.stackVersion?.trim()) {
    error('createPullRequest: stackVersion is empty. Review the artifacts-api for the branch ' + args.branchName)
  }
  sh(script: "${args.scriptFile} '${args.stackVersion}' 'true'", label: "Prepare changes for ${args.repo}")
  if (params.DRY_RUN_MODE) {
    log(level: 'INFO', text: "DRY-RUN: createPullRequest(repo: ${args.stackVersion}, labels: ${args.labels}, message: '${args.message}', base: '${args.branchName}', title: '${args.title}', assign: '${args.assign}', reviewer: '${args.reviewer}')")
    return
  }

  // If a similar PR was already created then do nothing.
  // This should avoid duplicated PRs when they have not been merged yet.
  // Though reusePullRequest argument allows to reuse existing open PRs.
  if (githubPrExists(args)) {
    log(level: 'INFO', text: 'A similar Pull Request already exists.')
    return
  }

  if (anyChangesToBeSubmitted("${args.branchName}")) {
    def arguments = [
      title: "${args.title}", labels: "${args.labels}", description: "${args.message}", base: "${args.branchName}"
    ]
    if (args.assign?.trim()) {
      arguments['assign'] = args.assign
    }
    if (args.reviewer?.trim()) {
      arguments['reviewer'] = args.reviewer
    }
    githubCreatePullRequest(arguments)
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

def reusePullRequestIfPossible(Map args = [:]){
  def title = args.title
  def pullRequests = githubPullRequests(labels: args.labels.split(','), titleContains: args.title)
  if (pullRequests && pullRequests.size() == 1) {
    pullRequests?.each { k, v ->
      log(level: 'INFO', text: "Reuse #${k} GitHub Pull Request.")
      gh(command: "pr checkout ${k}")
      gh(command: "pr edit ${k}", flags: [title: "${args.title}", body: "${args.message}"])
    }
    return true
  }
  log(level: 'INFO', text: 'Could not find a GitHub Pull Request. So fallback to create a new one instead.')
  return false
}

def findBranchName(Map args = [:]){
  def branch = args.branch
  // special macro to look for the latest minor version
  if (branch.contains('<minor>')) {
    def parts = branch.split('\\.')
    def major = parts[0]
    branch = args.versions.collect{ k,v -> k }.findAll { it ==~ /${major}\.\d+/ }.sort().last()
  }
  return branch
}

def createPRDescription(versionEntry) {
  return """### What \n Bump stack version with the latest one. \n ### Further details \n ${versionEntry}"""
}
