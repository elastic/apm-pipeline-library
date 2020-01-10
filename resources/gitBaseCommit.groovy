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

@Library('apm@git_base_commit') _

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
    PIPELINE_LOG_LEVEL='DEBUG'
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
    string(name: 'commmit_PR_332', defaultValue: 'fdcc03e0df0eccee1d55b0a7d1c2c3e16e79d682', description: '')
    string(name: 'commmit_PR_333', defaultValue: 'a93d2304c08473aa52d64ca38d3f3c6ceeeff2ec', description: '')
  }
  stages
  {
    stage('Test'){
      stages {
        stage('PR behind master') {
          options { skipDefaultCheckout() }
          steps {
            ws('pr-332'){
              deleteDir()
              script{
                gitCheckout(basedir: "${BASE_DIR}",
                  mergeTarget: "git_base_commit",
                  branch: 'pr/332',
                  repo: "git@github.com:elastic/${env.REPO}.git",
                  credentialsId: "${JOB_GIT_CREDENTIALS}",
                  githubNotifyFirstTimeContributor: false,
                  reference: "/var/lib/jenkins/${env.REPO}.git")
                def commit = "${params.commmit_PR_332}"
                dir("${BASE_DIR}"){
                  sh('''
                  git log -3
                  git branch
                  git branch -v
                  export
                  ''')
                }
                whenFalse(!env.GIT_COMMIT.equals(commit)){
                  error("GIT_COMMIT value is wrong expected different than ${commit} but found ${env.GIT_COMMIT}")
                }
                whenFalse(!env.GIT_SHA.equals(commit)){
                  error("GIT_SHA value is wrong expected different than ${commit} but found ${env.GIT_SHA}")
                }
                whenFalse(env.GIT_BASE_COMMIT.equals(commit)){
                  error("GIT_BASE_COMMIT value is wrong expected ${commit} but found ${env.GIT_BASE_COMMIT}")
                }
              }
            }
          }
        }
        stage('PR sync with master') {
          options { skipDefaultCheckout() }
          steps {
            ws('pr-333'){
              deleteDir()
              script{
                gitCheckout(basedir: "${BASE_DIR}",
                  mergeTarget: "git_base_commit",
                  branch: 'pr/332',
                  repo: "git@github.com:elastic/${env.REPO}.git",
                  credentialsId: "${JOB_GIT_CREDENTIALS}",
                  githubNotifyFirstTimeContributor: false,
                  reference: "/var/lib/jenkins/${env.REPO}.git")
                dir("${BASE_DIR}"){
                  sh('''
                  git log -3
                  git branch
                  git branch -v
                  export
                  ''')
                }
                commit = "${params.commmit_PR_333}"
                whenFalse(env.GIT_COMMIT.equals(commit)){
                  error("GIT_COMMIT value is wrong expected ${commit} but found ${env.GIT_COMMIT}")
                }
                whenFalse(env.GIT_SHA.equals(commit)){
                  error("GIT_SHA value is wrong expected ${commit} but found ${env.GIT_SHA}")
                }
                whenFalse(env.GIT_BASE_COMMIT.equals(commit)){
                  error("GIT_BASE_COMMIT value is wrong expected ${commit} but found ${env.GIT_BASE_COMMIT}")
                }
              }
            }
          }
        }
      }
    }
  }
}
