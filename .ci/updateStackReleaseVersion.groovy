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

@Library('apm@main') _

// To store the next and current release versions
@Field def releaseVersions = [:]

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
    booleanParam(name: 'FORCE', defaultValue: false, description: 'If true, skips the release version validation.')
  }
  stages {
    stage('Checkout') {
      steps {
        git(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
            url: "https://github.com/${ORG_NAME}/${REPO}.git",
            branch: 'main')
      }
    }
    stage('Fetch latest versions') {
      steps {
        fetchVersions()
      }
    }
    stage('Send Pull Request'){
      steps {
        createPullRequest(repo: env.REPO,
                          branchName: 'main',
                          labels: 'automation',
                          message: """### What \n Bump stack version with the latest one. \n ### Further details \n ${releaseVersions}""",
                          reviewer: 'elastic/observablt-robots-on-call',
                          stackVersions: releaseVersions,
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

def fetchVersions() {
  // To store all the latest release versions
  def latestReleaseVersions = artifactsApi(action: 'latest-release-versions')
  // To store all the latest release versions
  def latestVersions = artifactsApi(action: 'latest-versions')
  def current7 = latestReleaseVersions.findAll { it ==~ /7\.\d+\.\d+/ }.sort().last()
  def current8 = getCurrent8(latestReleaseVersions)
  // NOTE: 6 major branch is now EOL (we keep this for backward compatibility)
  releaseVersions[bumpUtils.current6Key()] = '6.8.23'
  releaseVersions[bumpUtils.current7Key()] = current7
  releaseVersions[bumpUtils.nextMinor7Key()] = increaseVersion(current7, 1)
  releaseVersions[bumpUtils.nextPatch7Key()] = increaseVersion(current7, 1)
  releaseVersions[bumpUtils.current8Key()] = current8
  releaseVersions[bumpUtils.nextMinor8Key()] = latestVersions.main.version.replaceAll('-SNAPSHOT','')
  releaseVersions[bumpUtils.nextPatch8Key()] = increaseVersion(current8, 1)
}

def getCurrent8(latestReleaseVersions) {
  def latestReleaseVersionsFor8 = latestReleaseVersions.findAll { it ==~ /8\.\d+\.\d+/ }.sort()
  def current = latestReleaseVersionsFor8.last()

  // If a new minor release (major.minor.0)
  // then we need to query the unified release to
  // know if the release is already available or no
  if (bumpUtils.getPatch(current) == '0') {
    def content = getBranchUnifiedRelease(bumpUtils.getMajorMinor(current))
    if (content.version == current) {
      // validate the releaseDate matches the current date in format
      // YYYY-mm-DD
      def currentDate = new Date()
      java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd")
      Date releaseDate = formatter.parse(content.releaseDate)
      if (currentDate.compareTo(releaseDate) > 0) {
        log(level: 'INFO', text: "getCurrent8: ${current} already released")
      } else {
        log(level: 'WARN', text: "getCurrent8: ${current} has not been released yet. Let's fallback to the previous last one.")
        // Get previous last one
        latestReleaseVersionsFor8.remove(latestReleaseVersionsFor8.size() - 1)
        current = latestReleaseVersionsFor8.last()
      }
    }
  }

  return current
}

def increaseVersion(version, i) {
  def minor = version.substring(version.lastIndexOf('.')+1)
  def m = (minor.toInteger() + i).toString()
  return version.substring(0,version.length()-1) + m
}

def createPullRequest(Map args = [:]) {
  bumpUtils.prepareContext(org: env.ORG_NAME, repo: args.repo, branchName: args.branchName)

  updateReleasesPropertiesFile(args)

  if (params.DRY_RUN_MODE) {
    log(level: 'INFO', text: "DRY-RUN: createPullRequest(repo: ${args.stackVersions}, labels: ${args.labels}, message: '${args.message}', base: '${args.branchName}', title: '${args.title}', reviewer: '${args.reviewer}')")
    return
  }

  // If a similar PR was already created then do nothing.
  // This should avoid duplicated PRs when they have not been merged yet.
  if (githubPrExists(args)) {
    log(level: 'INFO', text: 'A similar Pull Request already exists.')
    return
  }

  if (params.FORCE) {
    log(level: 'INFO', text: "Skip version validation.")
  } else {
    // In case docker images are not available yet, let's skip the PR automation.
    if (!bumpUtils.areStackVersionsAvailable(args.stackVersions)) {
      log(level: 'INFO', text: "Versions '${args.stackVersions}' are not available yet.")
      return
    }
  }

  if (bumpUtils.areChangesToBePushed("${args.branchName}")) {
    githubCreatePullRequest(bumpUtils.parseArguments(args))
  } else {
    log(level: 'INFO', text: "There are no changes to be submitted.")
  }
}

def updateReleasesPropertiesFile(Map args = [:]) {
  if (args?.stackVersions?.size() == 0) {
    error('updateReleasesPropertiesFile: stackVersions is empty. Review the artifacts-api for the branch ' + args.branchName)
  }
  // Update the properties file with the new releases
  writeFile file: 'resources/versions/releases.properties', text: """${bumpUtils.current6Key()}=${args.stackVersions.get(bumpUtils.current6Key())}
${bumpUtils.current7Key()}=${args.stackVersions.get(bumpUtils.current7Key())}
${bumpUtils.nextMinor7Key()}=${args.stackVersions.get(bumpUtils.nextMinor7Key())}
${bumpUtils.nextPatch7Key()}=${args.stackVersions.get(bumpUtils.nextPatch7Key())}
${bumpUtils.current8Key()}=${args.stackVersions.get(bumpUtils.current8Key())}
${bumpUtils.nextMinor8Key()}=${args.stackVersions.get(bumpUtils.nextMinor8Key())}
${bumpUtils.nextPatch8Key()}=${args.stackVersions.get(bumpUtils.nextPatch8Key())}"""

  // Prepare the changeset in git.
  sh(script: """
    git checkout -b "update-stack-release-version-\$(date "+%Y%m%d%H%M%S")-${args.branchName}"
    git add resources/versions/releases.properties
    git diff --staged --quiet || git commit -m "[automation] update elastic stack release versions to ${args.stackVersions.get(bumpUtils.current7Key())} and ${args.stackVersions.get(bumpUtils.nextMinor7Key())}"
    git --no-pager log -1""", label: "Git changes")
}
