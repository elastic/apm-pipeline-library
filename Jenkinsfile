#!/usr/bin/env groovy

@Library('apm@master') _

pipeline {
  agent any
  environment {
    BASE_DIR="src/github.com/elastic/apm-pipeline-library"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    PIPELINE_LOG_LEVEL = 'INFO'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  stages {
    stage('Initializing'){
      agent { label 'linux && immutable' }
      options { skipDefaultCheckout() }
      environment {
        PATH = "${env.PATH}:${env.WORKSPACE}/bin"
      }
      stages {
        /**
         Checkout the code and stash it, to use it on other stages.
        */
        stage('Checkout') {
          steps {
            deleteDir()
            gitCheckout(basedir: "${BASE_DIR}")
            stash allowEmpty: true, name: 'source', useDefaultExcludes: false
          }
        }
        /**
         Checkout the code and stash it, to use it on other stages.
        */
        stage('Test') {
          steps {
            deleteDir()
            unstash 'source'
            dir("${BASE_DIR}"){
              sh './mvnw clean test --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
            }
          }
          post {
            always {
              junit(allowEmptyResults: true,
                keepLongStdio: true,
                testResults: "${BASE_DIR}/target/surefire-reports/junit-*.xml,${BASE_DIR}/target/surefire-reports/TEST-*.xml")

                dir("${BASE_DIR}"){
                  emailext body: '''${SCRIPT, template="resources/groovy-html.template"}''',
                  mimeType: 'text/html',
                  subject: currentBuild.currentResult + " : 1 " + env.JOB_NAME,
                  //"Status: ${currentBuild.result?:'SUCCESS'} - Job \'${env.JOB_NAME}:${env.BUILD_NUMBER}\'",
                  attachLog: true,
                  compressLog: true,
                  recipientProviders: [brokenTestsSuspects(), brokenBuildSuspects(), upstreamDevelopers()],
                  to: "ivan.fernandez@elastic.co"
                }

                emailext body: '${SCRIPT, template="groovy-html.template"}',
                mimeType: 'text/html',
                subject: currentBuild.currentResult + " : 2 " + env.JOB_NAME,
                attachLog: true,
                compressLog: true,
                recipientProviders: [brokenTestsSuspects(), brokenBuildSuspects(), upstreamDevelopers()],
                to: "ivan.fernandez@elastic.co"
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
      step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "${NOTIFY_TO}", sendToIndividuals: false])
    }
    unstable {
      echoColor(text: '[UNSTABLE]', colorfg: 'yellow', colorbg: 'default')
    }
  }
}
