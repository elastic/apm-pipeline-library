#!/usr/bin/env groovy
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
  }
  parameters {
    string(name: 'PARAM_WITH_DEFAULT_VALUE', defaultValue: 'defaultValue', description: 'It would not be defined on the first build, see JENKINS-41929.')
  }
  stages {
    stage('Checkout') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        pipelineManager([ cancelPreviousRunningBuilds: [ when: 'PR' ] ])
        gitCheckout(basedir: "${BASE_DIR}", branch: 'master',
          repo: "git@github.com:elastic/${env.REPO}.git",
          credentialsId: "${JOB_GIT_CREDENTIALS}",
          githubNotifyFirstTimeContributor: false,
          reference: "/var/lib/jenkins/${env.REPO}.git")
        stash allowEmpty: true, name: 'source', useDefaultExcludes: false
      }
    }
    stage('Workers Checks'){
      parallel {
        stage('ubuntu && immutable check'){
          agent { label 'ubuntu && immutable' }
          options { skipDefaultCheckout() }
          environment {
            PATH = "${env.PATH}:${env.WORKSPACE}/bin:${env.WORKSPACE}/${BASE_DIR}/.ci/scripts"
            //see JENKINS-41929
            PARAM_WITH_DEFAULT_VALUE = "${params?.PARAM_WITH_DEFAULT_VALUE}"
          }
          stages {
            /**
            Build the project from code..
            */
            stage('Build') {
              steps {
                buildUnix()
              }
            }
            /**
            Execute unit tests.
            */
            stage('Test') {
              steps {
                testDockerInside()
                testUnix()
              }
              post {
                always {
                  junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
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
                  tag pattern: "v\\d+\\.\\d+\\.\\d+.*", comparator: 'REGEXP'
                  expression { return params.Run_As_Master_Branch }
                }
              }
              steps {
                echo "I am a tag or branch"
              }
            }
          }
          post {
            always {
              sh 'docker ps -a || true'
            }
          }
        }
        stage('windows 2019 immutable check'){
          agent { label 'windows-immutable && windows-2019' }
          options { skipDefaultCheckout() }
          stages {
            stage('Test') {
              steps {
                checkWindows()
              }
            }
            stage('Install tools') {
              options {
                warnError('installTools failed')
              }
              steps {
                installTools([ [tool: 'nodejs', version: '12' ] ])
              }
            }
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('MacOSX worker-c07l34n6dwym check'){
          agent { label 'worker-c07l34n6dwym' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testMac()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('MacOSX worker-c07y20b6jyvy check'){
          agent { label 'worker-c07y20b6jyvy' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testMac()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('MacOSX worker-c07ll940dwyl check'){
          agent { label 'worker-c07ll940dwyl' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testMac()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('MacOSX worker-c07y20b9jyvy check'){
          agent { label 'worker-c07y20b9jyvy' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testMac()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('MacOSX worker-c07y20b4jyvy check'){
          agent { label 'worker-c07y20b4jyvy' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testMac()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('MacOSX worker-c07y20bcjyvy check'){
          agent { label 'worker-c07y20bcjyvy' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testMac()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('BareMetal worker-395930 check'){
          agent { label 'worker-395930' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testBaremetal()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
            }
          }
        }
        stage('BareMetal arm worker-0a434dec4bdcd060f check'){
          agent { label 'worker-0a434dec4bdcd060f' }
          options { skipDefaultCheckout() }
          steps {
            buildUnix()
            testBaremetal()
          }
          post {
            always {
              junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
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

def testDockerInside(){
  docker.image('node:12').inside(){
    echo "Docker inside"
    dir("${BASE_DIR}"){
      withEnv(["HOME=${env.WORKSPACE}"]){
        sh(script: './resources/scripts/jenkins/build.sh')
      }
    }
  }
}

def buildUnix(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    sh returnStatus: true, script: './resources/scripts/jenkins/build.sh'
  }
}

def testUnix(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    // Ephemeral workers don't have a HOME env variable.
    withEnv(["HOME=${env.WORKSPACE}"]){
      sh returnStatus: true, script: './resources/scripts/jenkins/beats-ci/test.sh'
    }
  }
}

def testBaremetal(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    sh returnStatus: true, script: './resources/scripts/jenkins/beats-ci/test-baremetal.sh'
  }
}

def testMac(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    sh returnStatus: true, script: './resources/scripts/jenkins/beats-ci/test-mac.sh'
  }
}

def checkWindows(params = [:]){
  def withExtra = params.containsKey('withExtra') ? params.withExtra : false
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    powershell(script: ".\\resources\\scripts\\jenkins\\build.ps1 ${withExtra}")
  }
}

def checkOldWindows(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    bat(returnStatus: true, script: '.\\resources\\scripts\\jenkins\\build.bat')
  }
}
