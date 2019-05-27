#!/usr/bin/env groovy

@Library('apm@vcurrent') _

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
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
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
    booleanParam(name: 'doc_ci', defaultValue: true, description: 'Enable build docs.')
  }
  stages {
    stage('Initializing'){
      agent { label 'linux && immutable' }
      options { skipDefaultCheckout() }
      environment {
        PATH = "${env.PATH}:${env.WORKSPACE}/bin"
        ELASTIC_DOCS = "${env.WORKSPACE}/elastic/docs"
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
            //gitCheckout(basedir: "${BASE_DIR}")
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
                  expression { return params.Run_As_Master_Branch }
                }
                expression { return params.doc_ci }
              }
            }
            steps {
              deleteDir()
              unstash 'source'
              dir("${BASE_DIR}"){
                buildDocs(docsDir: "resources/docs", archive: true)
              }
            }
          }
        }
      }
      stage('windows 2012 check'){
        agent { label 'windows-2012r2' }
        options { skipDefaultCheckout() }
        steps {
          bat returnStatus: true, script: 'msbuild'
        }
      }
      stage('windows 2016 check'){
        agent { label 'windows-2016' }
        options { skipDefaultCheckout() }
        steps {
          bat returnStatus: true, script: 'msbuild'
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
