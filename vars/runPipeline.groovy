#!/usr/bin/env groovy

/**
  Run a pipeline passed as parameter.
  
  There is a limitation, the main pipeline should be definned in the call function.
  https://jenkins.io/doc/book/pipeline/shared-libraries/#defining-declarative-pipelines
*/
void call(Map args = [:]){
  def name = args.containsKey('name') ? args.name : 'default'
  switch (name) {
   case 'apm-ui': 
    pipeline {
     agent { label 'linux && immutable' }
     environment {
       BASE_DIR="src/github.com/elastic/kibana"
       ES_BASE_DIR="src/github.com/elastic/elasticsearch"
       JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
       FORCE_COLOR = "2"
     }
     options {
       timeout(time: 1, unit: 'HOURS')
       buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '2', daysToKeepStr: '30'))
       timestamps()
       preserveStashes()
       //see https://issues.jenkins-ci.org/browse/JENKINS-11752, https://issues.jenkins-ci.org/browse/JENKINS-39536, https://issues.jenkins-ci.org/browse/JENKINS-54133 and jenkinsci/ansicolor-plugin#132
       ansiColor('xterm')
       disableResume()
       durabilityHint('PERFORMANCE_OPTIMIZED')
     }
     parameters {
       string(name: 'GIT_URL', defaultValue: "https://github.com/elastic/kibana.git", description: "Repo")
       string(name: 'ES_GIT_URL', defaultValue: "https://github.com/elastic/elasticsearch.git", description: "Repo")
       string(name: 'branch_specifier', defaultValue: "master", description: "the Git branch specifier to build (branchName, tagName, commitId, etc.)")
       string(name: 'TEST_BROWSER_HEADLESS', defaultValue: "1", description: "Use headless browser.")
       string(name: 'TEST_ES_FROM', defaultValue: "source", description: "Test from sources.")
       booleanParam(name: 'Run_As_Master_Branch', defaultValue: false, description: 'Allow to run any steps on a PR, some steps normally only run on master branch.')
       booleanParam(name: 'test_ci', defaultValue: true, description: 'Enable test')
       booleanParam(name: 'build_oss_ci', defaultValue: false, description: 'Build OSS')
       booleanParam(name: 'build_no_oss_ci', defaultValue: false, description: 'Build NO OSS')
       booleanParam(name: 'intake_ci', defaultValue: false, description: 'Intake Tests')
       booleanParam(name: 'ciGroup_ci', defaultValue: false, description: 'Group Tests')
       booleanParam(name: 'x_pack_intake_ci', defaultValue: false, description: 'X-Pack intake Tests')
       booleanParam(name: 'x_pack_ciGroup_ci', defaultValue: false, description: 'X-Pack Group Tests')
     }
     stages {
       /**
        Checkout the code and stash it, to use it on other stages.
       */
       stage('Initializing') {
         agent { label 'linux && immutable' }
         environment {
           HOME = "${env.WORKSPACE}"
         }
         steps {
           script { pipelineApmUI.checkoutSteps() }
         }
       }
       stage('build'){
         failFast true
         parallel {
           /**
           Build on a linux environment.
           */
           stage('build oss') {
             agent { label 'linux && immutable' }
             when {
               beforeAgent true
               environment name: 'build_oss_ci', value: 'true'
             }
             steps {
               script { pipelineApmUI.buildOSSSteps() }
             }
           }
           /**
           Building and extracting default Kibana distributable for use in functional tests
           */
           stage('build no-oss') {
             agent { label 'linux && immutable' }
             when {
               beforeAgent true
               environment name: 'build_no_oss_ci', value: 'true'
             }
             steps {
               script { pipelineApmUI.buildNoOSSSteps() }
             }
           }
         }
       }
       /**
       Test on a linux environment.
       */
       stage('kibana-intake') {
         when {
           beforeAgent true
           environment name: 'intake_ci', value: 'true'
         }
         steps {
           script { pipelineApmUI.kibanaIntakeSteps() }
         }
         post { always { grabTestResults() } }
       }
       /**
       Test ciGroup tests on a linux environment.
       */
       stage('kibana-ciGroup') {
         when {
           beforeAgent true
           environment name: 'ciGroup_ci', value: 'true'
         }
         steps {
           script { pipelineApmUI.kibanaGroupSteps() }
         }
         post { always { grabTestResults() } }
       }
       /**
       Test x-pack-intake tests on a linux environment.
       */
       stage('x-pack-intake') {
         environment {
           XPACK_DIR = "${env.WORKSPACE}/${env.BASE_DIR}/x-pack"
         }
         when {
           beforeAgent true
           environment name: 'x_pack_intake_ci', value: 'true'
         }
         steps {
           script { pipelineApmUI.xPackIntakeSteps() }
         }
         post { always { grabTestResults() } }
       }
       /**
       Test x-pack-ciGroup tests on a linux environment.
       */
       stage('x-pack-ciGroup') {
         environment {
           XPACK_DIR = "${env.WORKSPACE}/${env.BASE_DIR}/x-pack"
           INSTALL_DIR = "${env.WORKSPACE}/install/kibana"
         }
         when {
           beforeAgent true
           environment name: 'x_pack_ciGroup_ci', value: 'true'
         }
         steps {
           script { pipelineApmUI.xPackGroupSteps() }
         }
         post { always { grabTestResults() } }
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
    break
  case 'test': 
    pipeline {
      agent { label 'linux' }
      stages {
        stage('Hello'){
          steps {
            echo "Hello, I am Test pipeline"
          }
        }
      }
    }
    break
   default: 
    pipeline {
     agent { label 'linux' }
     stages {
       stage('Hello'){
         steps {
           echo "Hello, I am pipeline"
         }
       }
     }
    }
  }
}
