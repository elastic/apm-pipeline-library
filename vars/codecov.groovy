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

/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/

@Field def tokens = [:]

def call(Map args = [:]){
  if(!isUnix()){
    error('codecov: windows is not supported yet.')
  }
  def repo = args?.repo
  def basedir = args.containsKey('basedir') ? args.basedir : "."
  def flags = args.containsKey("flags") ? args.flags : ""
  def secret = args?.secret

  if(!repo){
    log(level: 'WARN', text: "Codecov: No repository specified.")
    return
  }

  def token = null
  if(tokens["${repo}"] == null){
    log(level: 'DEBUG', text: "Codecov: get the token from Vault.")
    if(secret != null){
      token = getVaultSecret(secret: secret)?.data?.value
    } else {
      /** TODO remove it is only for APM projects */
      token = getVaultSecret("${repo}-codecov")?.data?.value
    }
    tokens["${repo}"] = token
  } else {
    log(level: 'DEBUG', text: "Codecov: get the token from cache.")
    token = tokens["${repo}"]
  }
  if(!token){
    log(level: 'WARN', text: "Codecov: Repository not found: ${repo}")
    return
  }

  dir(basedir){
    log(level: 'INFO', text: "Codecov: Getting branch ref...")
    def branchName = githubBranchRef()
    if(branchName == null){
      error "Codecov: was not possible to get the branch ref"
    }
    log(level: 'INFO', text: "Codecov: Sending data...")
    // Set some env variables so codecov detection script works correctly
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
      [var: 'CODECOV_TOKEN', password: "${token}"],
      ]]) {
      withEnv([
        "ghprbPullId=${env.CHANGE_ID}",
        "GIT_BRANCH=${branchName}",
        "CODECOV_TOKEN=${token}"]) {
        retryWithSleep(retries: 3, seconds: 5, backoff: true) {
          sh(label: 'Download Codecov', script: """#!/bin/bash
          set -x
          curl -sSLo codecov.sh https://codecov.io/bash
          """)
        }
        sh label: 'Send report to Codecov', script: """#!/bin/bash
        set -x
        bash codecov.sh ${flags} || echo "codecov exited with \$?"
        """
      }
    }
  }
}
