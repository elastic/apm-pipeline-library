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
    BASE_DIR="src/github.com/elastic/PROJECT"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
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
  triggers {
    cron 'H H(3-4) * * 1-5'
    issueCommentTrigger('(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'PARAM_WITH_DEFAULT_VALUE', defaultValue: "defaultValue", description: "it would not be defined on the first build, see JENKINS-41929.")
    booleanParam(name: 'Run_As_Master_Branch', defaultValue: false, description: 'Allow to run any steps on a PR, some steps normally only run on master branch.')
  }
  stages {
    stage('Workers Checks'){
      parallel {
        stage('linux && immutable check'){
          agent { label 'linux && immutable' }
          options { skipDefaultCheckout() }
          environment {
            PATH = "${env.PATH}:${env.WORKSPACE}/bin:${env.WORKSPACE}/${BASE_DIR}/.ci/scripts"
            //see JENKINS-41929
            PARAM_WITH_DEFAULT_VALUE = "${params?.PARAM_WITH_DEFAULT_VALUE}"
          }
          stages {
            /**
            Checkout the code and stash it, to use it on other stages.
            */
            stage('Checkout') {
              steps {
                deleteDir()
                gitCheckout(basedir: "${BASE_DIR}", branch: 'master',
                  repo: 'git@github.com:elastic/apm-pipeline-library.git',
                  credentialsId: "${JOB_GIT_CREDENTIALS}")
                stash allowEmpty: true, name: 'source', useDefaultExcludes: false
              }
            }
            /**
            Build the project from code..
            */
            stage('Build') {
              steps {
                deleteDir()
                unstash 'source'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/build.sh'
                }
              }
            }
            /**
            Execute unit tests.
            */
            stage('Test') {
              steps {
                deleteDir()
                unstash 'source'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/test.sh'
                }
              }
              post {
                always {
                  junit(allowEmptyResults: true,
                    keepLongStdio: true,
                    testResults: "${BASE_DIR}/**/junit-*.xml,${BASE_DIR}/target/**/TEST-*.xml"
                  )
                }
              }
            }
            stage('Run on branch or tag'){
              when {
                beforeAgent true
                anyOf {
                  branch 'master'
                  branch "v7*"
                  branch "v8*"
                  tag pattern: "v\\d+\\.\\d+\\.\\d+*", comparator: "REGEXP"
                  expression { return params.Run_As_Master_Branch }
                }
              }
              steps {
                echo "I am a tag or branch"
              }
            }
          }
        }
        stage('Ubuntu 18.04 test'){
          agent { label 'ubuntu-edge' }
          options { skipDefaultCheckout() }
          environment {
            PATH = "${env.PATH}:${env.WORKSPACE}/bin:${env.WORKSPACE}/${BASE_DIR}/.ci/scripts"
            //see JENKINS-41929
            PARAM_WITH_DEFAULT_VALUE = "${params?.PARAM_WITH_DEFAULT_VALUE}"
          }
          stages {
            /**
            Checkout the code and stash it, to use it on other stages.
            */
            stage('Checkout') {
              steps {
                deleteDir()
                gitCheckout(basedir: "${BASE_DIR}", branch: 'master',
                  repo: 'git@github.com:elastic/apm-pipeline-library.git',
                  credentialsId: "${JOB_GIT_CREDENTIALS}")
                stash allowEmpty: true, name: 'source', useDefaultExcludes: false
              }
            }
            /**
            Build the project from code..
            */
            stage('Build') {
              steps {
                deleteDir()
                unstash 'source'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/build.sh'
                }
              }
            }
            /**
            Execute unit tests.
            */
            stage('Test') {
              steps {
                deleteDir()
                unstash 'source'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/test.sh'
                }
              }
              post {
                always {
                  junit(allowEmptyResults: true,
                    keepLongStdio: true,
                    testResults: "${BASE_DIR}/**/junit-*.xml,${BASE_DIR}/target/**/TEST-*.xml")
                  }
                }
              }
            }
        }

        stage('Debian 9 test'){
          agent { label 'debian-9' }
          options { skipDefaultCheckout() }
          environment {
            PATH = "${env.PATH}:${env.WORKSPACE}/bin:${env.WORKSPACE}/${BASE_DIR}/.ci/scripts"
            //see JENKINS-41929
            PARAM_WITH_DEFAULT_VALUE = "${params?.PARAM_WITH_DEFAULT_VALUE}"
          }
          stages {
            /**
            Checkout the code and stash it, to use it on other stages.
            */
            stage('Checkout') {
              steps {
                deleteDir()
                gitCheckout(basedir: "${BASE_DIR}", branch: 'master',
                  repo: 'git@github.com:elastic/apm-pipeline-library.git',
                  credentialsId: "${JOB_GIT_CREDENTIALS}")
                stash allowEmpty: true, name: 'source', useDefaultExcludes: false
              }
            }
            /**
            Build the project from code..
            */
            stage('Build') {
              steps {
                deleteDir()
                unstash 'source'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/build.sh'
                }
              }
            }
            /**
            Execute unit tests.
            */
            stage('Test') {
              steps {
                deleteDir()
                unstash 'source'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/test.sh'
                }
              }
              post {
                always {
                  junit(allowEmptyResults: true,
                    keepLongStdio: true,
                    testResults: "${BASE_DIR}/**/junit-*.xml,${BASE_DIR}/target/**/TEST-*.xml")
                  }
                }
              }
            }
        }
        stage('windows 2012 immutable check'){
          agent { label 'windows-2012-r2-immutable' }
          options { skipDefaultCheckout() }
          steps {
            bat returnStatus: true, script: 'msbuild'
            bat returnStatus: true, script: 'dotnet --info'
            bat returnStatus: true, script: 'nuget --help'
            bat returnStatus: true, script: 'vswhere'
            bat returnStatus: true, script: 'docker -v'
          }
        }
        stage('windows 2016 immutable check'){
          agent { label 'windows-2016-immutable' }
          options { skipDefaultCheckout() }
          steps {
            bat returnStatus: true, script: 'msbuild'
            bat returnStatus: true, script: 'dotnet --info'
            bat returnStatus: true, script: 'nuget --help'
            bat returnStatus: true, script: 'vswhere'
            bat returnStatus: true, script: 'docker -v'
          }
        }
        stage('windows 2019 immutable check'){
          agent { label 'windows-2019-immutable' }
          options { skipDefaultCheckout() }
          steps {
            bat returnStatus: true, script: 'msbuild'
            bat returnStatus: true, script: 'dotnet --info'
            bat returnStatus: true, script: 'nuget --help'
            bat returnStatus: true, script: 'vswhere'
            bat returnStatus: true, script: 'docker -v'
          }
        }
        stage('windows 2019 docker immutable check'){
          agent { label 'windows-2019-docker-immutable' }
          options { skipDefaultCheckout() }
          steps {
            bat returnStatus: true, script: 'msbuild'
            bat returnStatus: true, script: 'dotnet --info'
            bat returnStatus: true, script: 'nuget --help'
            bat returnStatus: true, script: 'vswhere'
            bat returnStatus: true, script: 'docker -v'
          }
        }
        stage('Mac OS X check'){
          stages {
            stage('Checkout') {
              steps {
                deleteDir()
                gitCheckout(basedir: "${BASE_DIR}", branch: 'master',
                  repo: 'git@github.com:elastic/apm-pipeline-library.git',
                  credentialsId: "${JOB_GIT_CREDENTIALS}")
                stash allowEmpty: true, name: 'source-mac', useDefaultExcludes: false
              }
            }
            stage('build') {
              agent { label 'macosx' }
              options { skipDefaultCheckout() }
              steps {
                deleteDir()
                unstash 'source-mac'
                dir("${BASE_DIR}"){
                  sh returnStatus: true, script: './resources/scripts/jenkins/build.sh'
                }
              }
            }
          }
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
