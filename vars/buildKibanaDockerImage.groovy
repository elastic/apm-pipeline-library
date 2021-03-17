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

import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper
import hudson.model.Result
import co.elastic.BuildException

/**

  Builds the Docker image for Kibana, from a branch or a pull Request.

  buildKibanaDockerImage(branch: 'master')
  buildKibanaDockerImage(branch: 'PR/12345')
*/
def call(Map args = [:]){
  def branch = args?.branch?.trim() ? args.branch : 'master'
  def uppercaseBranch = branch.toUpperCase()

  def kibanaBranch = branch
  if (uppercaseBranch.startsWith('PR/')) {
    kibanaBranch = uppercaseBranch
  }
  log(level: 'INFO', text: "Kibana branch is: ${kibanaBranch}")

  //buildKibana(branch: "${kibanaBranch}")
}

def buildKibana(Map args = [:]) {
  def branch = args.branch
  def deployName = normalize(branch)
  def kibanaDockerTargetTag = !isEmptyString(args.targetTag) ? args.targetTag : getGitCommitSha()

  // constant values
  def dockerRegistry = 'docker.elastic.co'
  def dockerRegistrySecret = 'secret/observability-team/ci/docker-registry/prod'
  def dockerImageSource = "${dockerRegistry}/kibana/kibana"
  def dockerImageTarget = "${dockerRegistry}/observability-ci/kibana"

  log(level: 'INFO', text: "Cloning Kibana repository, branch ${branch}")

  checkout([$class: 'GitSCM',
    branches: [[name: "*/${branch}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [
      [$class: 'RelativeTargetDirectory', relativeTargetDir: "${env.BASE_DIR}"],
      [$class: 'CheckoutOption', timeout: 15],
      [$class: 'AuthorInChangelog'],
      [$class: 'IgnoreNotifyCommit'],
      [$class: 'CloneOption',
        depth: 0,
        noTags: false,
        reference: "/var/lib/jenkins/kibana.git",
        shallow: true,
        timeout: 15
      ]],
      submoduleCfg: [],
      userRemoteConfigs: [[
        credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
        url: "http://github.com/elastic/kibana.git",
        refspec: '+refs/heads/*:refs/remotes/origin/* +refs/pull/*/head:refs/remotes/origin/PR/*',
        ]]
  ])

  dir("${env.BASE_DIR}"){
    setEnvVar('NODE_VERSION', readFile(file: ".node-version")?.trim())
    setEnvVar('KIBANA_DOCKER_TAG', readJSON(file: 'package.json').version + '-SNAPSHOT')

    retry(3){
      sh(label: 'Build Docker image', script: '''#!/bin/bash
        set -x
        unset NVM_DIR
        if [ -z "$(command -v nvm)" ]; then
          curl -Sso- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.3/install.sh | bash
          export NVM_DIR="$HOME/.nvm"
          [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
        fi
        if [ "v${NODE_VERSION}" != "$(node --version)" ]; then
          . "${NVM_DIR}/nvm.sh"
          nvm install "${NODE_VERSION}"
          nvm use "${NODE_VERSION}"
        fi
        export NODE_OPTIONS=" --max-old-space-size=4096"
        export FORCE_COLOR=1
        export BABEL_DISABLE_CACHE=true
        npm install -g yarn
        yarn kbn clean
        yarn kbn bootstrap
        node scripts/build --no-debug --no-oss --skip-docker-ubi --docker-images
      ''')
    }
  }

  dockerLogin(secret: "${dockerRegistrySecret}", registry: "${dockerRegistry}")
  retry(3){
    sh(label: 'Push Docker image', script: """
      docker tag ${dockerImageSource}:${KIBANA_DOCKER_TAG} ${dockerImageTarget}:${kibanaDockerTargetTag}
      docker tag ${dockerImageSource}:${KIBANA_DOCKER_TAG} ${dockerImageTarget}:${deployName}
      docker push ${dockerImageTarget}:${kibanaDockerTargetTag}
      docker push ${dockerImageTarget}:${deployName}
    """)
  }
}

def isEmptyString(value){
  return value == null || value?.trim() == ""
}

def normalize(value){
  return value?.trim().toLowerCase().replaceAll("[^A-Za-z0-9 ]", "")
}
