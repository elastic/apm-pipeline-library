#!/usr/bin/env groovy

pipeline {
  agent none
  environment {
    BASE_DIR="src/github.com/elastic/PROJECT"
  }
  options {
    timeout(time: 1, unit: 'HOURS') 
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  parameters {
    booleanParam(name: 'Run_As_Master_Branch', defaultValue: false, description: 'Allow to run any steps on a PR, some steps normally only run on master branch.')
  }
  stages {
    stage('Initializing'){
      agent { label 'linux && immutable' }
      options { skipDefaultCheckout() }
      environment {
        PATH = "${env.PATH}:${env.WORKSPACE}/bin"
        ELASTIC_DOCS = "${env.WORKSPACE}/elastic/docs"
      }
      stages {
        /**
         Checkout the code and stash it, to use it on other stages.
        */
        stage('Checkout') {
          steps {
            gitCheckout(basedir: "${BASE_DIR}")
            stash allowEmpty: true, name: 'source', useDefaultExcludes: false
          }
        }
        /**
         Build the project from code..
        */
        stage('Build') {
          steps {
            withEnvWrapper() {
              unstash 'source'
              dir("${BASE_DIR}"){
                sh './scripts/jenkins/build.sh'
              }
            }
          }
        }
        /**
         Execute unit tests.
        */
        stage('Test') {
          steps {
            withEnvWrapper() {
              unstash 'source'
              dir("${BASE_DIR}"){
                sh './scripts/jenkins/test.sh'
              }
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
        /**
          Build the documentation.
        */
        stage('Documentation') {
          when {
            beforeAgent true
            allOf {
              anyOf {
                not {
                  changeRequest()
                }
                branch 'master'
                branch "\\d+\\.\\d+"
                branch "v\\d?"
                tag "v\\d+\\.\\d+\\.\\d+*"
                environment name: 'Run_As_Master_Branch', value: 'true'
              }
              environment name: 'doc_ci', value: 'true'
            }
          }
          steps {
            withEnvWrapper() {
              unstash 'source'
              checkoutElasticDocsTools(basedir: "${ELASTIC_DOCS}")
              dir("${BASE_DIR}"){
                sh './scripts/jenkins/docs.sh'
              }
            }
          }
          post{
            success {
              tar(file: "doc-files.tgz", archive: true, dir: "html", pathPrefix: "${BASE_DIR}/docs")
            }
          }
        }
      }
    }
  }
  post { 
    success {
      echoColor(text: '[SUCCESS]', colorfg: 'green', colorbg: 'default')
    }
    aborted {
      echoColor(text: '[ABORTED]', colorfg: 'magenta', colorbg: 'default')
    }
    failure { 
      echoColor(text: '[FAILURE]', colorfg: 'red', colorbg: 'default')
      //step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "${NOTIFY_TO}", sendToIndividuals: false])
    }
    unstable { 
      echoColor(text: '[UNSTABLE]', colorfg: 'yellow', colorbg: 'default')
    }
  }
}