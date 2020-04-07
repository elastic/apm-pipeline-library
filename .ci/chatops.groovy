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

@Library('apm@current') _

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    PIPELINE_LOG_LEVEL = 'INFO'
    BRANCH_NAME = "${params.branch_specifier}"
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
    issueCommentTrigger('(?i)^\\/run(-\\w+)?$')
  }
  parameters {
    string(name: 'branch_specifier', defaultValue: "master", description: "the Git branch specifier to build")
  }
  stages {
    /**
     Checkout the code and stash it, to use it on other stages.
    */
    stage('Checkout') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        checkout scm
        /*
        gitCheckout(basedir: "${BASE_DIR}", branch: "${params.branch_specifier}",
          repo: "git@github.com:elastic/${env.REPO}.git",
          credentialsId: "${JOB_GIT_CREDENTIALS}",
          githubNotifyFirstTimeContributor: false,
          reference: "/var/lib/jenkins/${env.REPO}.git")
        */
        matcher()
      }
    }
  }
}

def matcher(){
  switch ("${env.GITHUB_COMMENT}") {
    case ~/\/run/:
        runCmd()
        break
    case ~/\/run-lint/:
        lint()
        break
    case ~/\/run-test/:
        test()
        break
    case ~/\/run-build/:
        build()
        break
    case ~/\/run-deploy/:
        deploy()
        break
    default:
        echo "Unrecognized..."
  }
}

def runCmd(){
  echo "${env.GITHUB_COMMENT}"
}

def lint(){
  echo "${env.GITHUB_COMMENT}"
}

def test(){
  echo "${env.GITHUB_COMMENT}"
}

def build(){
  echo "${env.GITHUB_COMMENT}"
}

def deploy(){
  echo "${env.GITHUB_COMMENT}"
}
