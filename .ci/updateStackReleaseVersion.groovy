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

// To store the next and current release versions
@Field def releaseVersions

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
        echo ' TODO: calculate the versions'
      }
    }
    stage('Send Pull Request'){
      steps {
        createPullRequest(repo: env.REPO,
                          branchName: 'master',
                          labels: 'automation',
                          reviewer: 'elastic/observablt-robots-on-call',
                          title: '[automation] Update Elastic stack release version source of truth')
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def createPullRequest(Map args = [:]) {
  def message = createPRDescription(latestVersions)
  prepareContext(repo: args.repo, branchName: args.branchName)
  if (args?.stackVersions?.size() == 0) {
    error('createPullRequest: stackVersions is empty. Review the artifacts-api for the branch ' + args.branchName)
  }
  writeFile file: 'resources/versions/releases.properties', text: """current_6=${releaseVersions.get('current.6')}
current_7=${releaseVersions.get('current.7')}
next_minor_7=${releaseVersions.get('next.minor.7')}
next_patch_7=${releaseVersions.get('next.patch.7')}"""
  sh(script: """
  git checkout -b "update-release-version-\$(date "+%Y%m%d%H%M%S")-${args.branchName}"
  git add resources/versions/releases.properties
  git diff --staged --quiet || git commit -m "[Automation] Update elastic stack release versions to ${VERSION_RELEASE} and ${VERSION_DEV}"
  git --no-pager log -1
  """, label: "Git changes")

  if (params.DRY_RUN_MODE) {
    log(level: 'INFO', text: "DRY-RUN: createPullRequest(repo: ${args.stackVersions}, labels: ${args.labels}, message: '${message}', base: '${args.branchName}', title: '${args.title}', reviewer: '${args.reviewer}')")
    return
  }

  // If a similar PR was already created then do nothing.
  // This should avoid duplicated PRs when they have not been merged yet.
  if (githubPrExists(args)) {
    log(level: 'INFO', text: 'A similar Pull Request already exists.')
    return
  }

  // In case docker images are not available yet, let's skip the PR automation.
  if (!areVersionsAvailable(args.stackVersions)) {
    log(level: 'INFO', text: "Versions '${args.stackVersions}' are not available yet.")
    return
  }

  if (anyChangesToBeSubmitted("${args.branchName}")) {
    def arguments = [
      title: "${args.title}", labels: "${args.labels}", description: "${message}", base: "${args.branchName}"
    ]
    if (args.reviewer?.trim()) {
      arguments['reviewer'] = args.reviewer
    }
    githubCreatePullRequest(arguments)
  } else {
    log(level: 'INFO', text: "There are no changes to be submitted.")
  }
}

def areVersionsAvailable(stackVersions) {
  if(stackVersions.size() >= 2){
    return dockerImageExists(image: "docker.elastic.co/elasticsearch/elasticsearch:${stackVersions[stackVersions.size() - 2]}") &&
           dockerImageExists(image: "docker.elastic.co/elasticsearch/elasticsearch:${stackVersions[stackVersions.size() - 1]}")
  } else if (stackVersions.size() == 1) {
    return dockerImageExists(image: "docker.elastic.co/elasticsearch/elasticsearch:${stackVersions[0]}")
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
  return """### What \n Update current and upcoming release details. \n ### Further details \n ${versionEntry}"""
}
