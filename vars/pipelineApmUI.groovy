/**
  APM UI Pipeline
*/
void call(Map args = [:]){
  pipeline {
   agent { label 'linux && immutable' }
   environment {
     BASE_DIR="src/github.com/elastic/kibana"
     ES_BASE_DIR="src/github.com/elastic/elasticsearch"
     JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
     FORCE_COLOR = "2"
     GIT_URL = "git@github.com:elastic/kibana.git"
     ES_GIT_URL = "git@github.com:elastic/elasticsearch.git"
     TEST_BROWSER_HEADLESS = "${params.TEST_BROWSER_HEADLESS}"
     TEST_ES_FROM = "${params.TEST_ES_FROM}"
   }
   options {
     timeout(time: 1, unit: 'HOURS')
     buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '2', daysToKeepStr: '30'))
     timestamps()
     preserveStashes()
     ansiColor('xterm')
     disableResume()
     durabilityHint('PERFORMANCE_OPTIMIZED')
   }
   parameters {
     string(name: 'branch_specifier', defaultValue: "6.5", description: "the Git branch specifier to build (branchName, tagName, commitId, etc.)")
     string(name: 'ES_VERSION', defaultValue: "6.5", description: "Elastic Stack Git branch/tag to use")
     string(name: 'TEST_BROWSER_HEADLESS', defaultValue: "1", description: "Use headless browser.")
     string(name: 'TEST_ES_FROM', defaultValue: "source", description: "Test from sources.")
     booleanParam(name: 'Run_As_Master_Branch', defaultValue: false, description: 'Allow to run any steps on a PR, some steps normally only run on master branch.')
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
         //checkoutSteps()
         echo "NOOP"
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
             expression { return params.build_oss_ci }
           }
           steps {
             buildOSSSteps()
           }
         }
         /**
         Building and extracting default Kibana distributable for use in functional tests
         */
         stage('build no-oss') {
           agent { label 'linux && immutable' }
           when {
             beforeAgent true
             expression { return params.build_no_oss_ci }
           }
           steps {
             buildNoOSSSteps()
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
         expression { return params.intake_ci }
       }
       steps {
         kibanaIntakeSteps()
       }
       post { always { grabTestResults() } }
     }
     /**
     Test ciGroup tests on a linux environment.
     */
     stage('kibana-ciGroup') {
       when {
         beforeAgent true
         expression { return params.ciGroup_ci }
       }
       steps {
         kibanaGroupSteps()
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
         expression { return params.x_pack_intake_ci }
       }
       steps {
         xPackIntakeSteps()
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
         expression { return params.x_pack_ciGroup_ci }
       }
       steps {
         xPackGroupSteps()
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
}

def grabTestResults(){
  junit(allowEmptyResults: true,
    keepLongStdio: true,
    testResults: "${BASE_DIR}/**/target/junit/**/*.xml")
  archiveArtifacts(allowEmptyArchive: true,
    artifacts: "${BASE_DIR}/x-pack/test/functional/apps/reporting/reports/session/*.pdf,${BASE_DIR}/x-pack/test/functional/failure_debug/html/*.html,${BASE_DIR}/x-pack/test/**/screenshots/**/*.png,${BASE_DIR}/test/functional/failure_debug/html/*.html,${BASE_DIR}/test/**/screenshots/**/*.png,${BASE_DIR}/target/kibana-*",
    onlyIfSuccessful: false)
}

def nodeEnviromentVars(nodeVersion){
  /** TODO this enviroment variables could change on diferent type of agents, so maybe it is better to move then to the stage*/
  if(env.ORG_PATH == null){
    env.ORG_PATH = env.PATH
  }
  env.NODE_DIR="${WORKSPACE}/node/${nodeVersion}"
  env.NODE_BIN="${NODE_DIR}/bin"
  env.PATH="${NODE_BIN}:${WORKSPACE}/${BASE_DIR}/node_modules/.bin:${NODE_DIR}/lib/node_modules/yarn/bin:${ORG_PATH}"
  sh 'export'
}

def installNodeJs(nodeVersion, pakages = null){
  nodeEnviromentVars(nodeVersion)
  sh """#!/bin/bash
  set -euxo pipefail
  NODE_URL="https://nodejs.org/dist/v${nodeVersion}/node-v${nodeVersion}-linux-x64.tar.gz"
  mkdir -p "${NODE_DIR}"
  curl -sL \${NODE_URL} | tar -xz -C "${NODE_DIR}" --strip-components=1
  node --version
  npm config set prefix "${NODE_DIR}"
  npm config list
  """
  def cmd = "echo 'Installing aditional packages'\n"
  pakages?.each{ pkg ->
    cmd += "npm install -g ${pkg}\n"
  }
  sh """#!/bin/bash
  set -euxo pipefail
  ${cmd}
  """
}

def checkoutES(){
  dir("${ES_BASE_DIR}"){
    checkout([$class: 'GitSCM', branches: [[name: "${params.ES_VERSION}"]],
      doGenerateSubmoduleConfigurations: false,
      extensions: [],
      submoduleCfg: [],
      userRemoteConfigs: [[credentialsId: "${JOB_GIT_CREDENTIALS}",
      url: "${ES_GIT_URL}"]]])
  }
}

def checkoutSteps(){
  sh 'export'
  withEnvWrapper() {
    gitCheckout(basedir: "${BASE_DIR}", branch: params.branch_specifier, 
      repo: "${GIT_URL}", 
      credentialsId: "${JOB_GIT_CREDENTIALS}")
    //stash allowEmpty: true, name: 'source', excludes: ".git", useDefaultExcludes: false
    dir("${BASE_DIR}"){
      script{  
        def packageJson = readJSON(file: 'package.json')
        env.NODE_VERSION = packageJson.engines.node
        env.YARN_VERSION = packageJson.engines.yarn
        installNodeJs("${NODE_VERSION}", ["yarn@${YARN_VERSION}"])
        sh '''#!/bin/bash
        set -euxo pipefail
        PATH=${PATH}:$(yarn bin)
        yarn kbn bootstrap
        '''
      }
    }
    //stash allowEmpty: true, name: 'source', excludes: ".git,node/**", useDefaultExcludes: false
    //stash allowEmpty: true, name: 'cache', includes: "${BASE_DIR}/node_modules/**,node/**", useDefaultExcludes: false
    // dir("${ES_BASE_DIR}"){
    //   checkout([$class: 'GitSCM', branches: [[name: "${params.ES_VERSION}"]],
    //     doGenerateSubmoduleConfigurations: false,
    //     extensions: [],
    //     submoduleCfg: [],
    //     userRemoteConfigs: [[credentialsId: "${JOB_GIT_CREDENTIALS}",
    //     url: "${ES_GIT_URL}"]]])
    // }
    //stash allowEmpty: true, name: 'es', includes: "${ES_BASE_DIR}/**", excludes: ".git", useDefaultExcludes: false
  }
}

def buildOSSSteps(){
  withEnvWrapper() {
    //unstash 'source'
    //unstash 'cache'
    checkoutSteps()
    dir("${BASE_DIR}"){
      sh '''#!/bin/bash
      set -euxo pipefail
      PATH=${PATH}:$(yarn bin)
      node scripts/build --debug --oss --skip-archives --skip-os-packages
      '''
    }
    stash allowEmpty: true, name: 'build-oss', includes: "${BASE_DIR}/build/**", useDefaultExcludes: false
  }
}

def buildNoOSSSteps(){
  withEnvWrapper() {
    //unstash 'source'
    //unstash 'cache'
    checkoutSteps()
    dir("${BASE_DIR}"){
      sh '''#!/bin/bash
      set -euxo pipefail
      PATH=${PATH}:$(yarn bin)
      node scripts/build --debug --no-oss --skip-os-packages
      '''
      sh '''#!/bin/bash
      set -euxo pipefail
      PATH=${PATH}:$(yarn bin)
      linuxBuild="$(find "./target" -name 'kibana-*-linux-x86_64.tar.gz')"
      installDir="${WORKSPACE}/install/kibana"
      mkdir -p "${installDir}"
      tar -xzf "${linuxBuild}" -C "${installDir}" --strip=1
      '''
    }
    stash allowEmpty: true, name: 'kibana-bin', includes: "install/kibana/**", useDefaultExcludes: false
    stash allowEmpty: true, name: 'build-no-oss', includes: "${BASE_DIR}/build/**", useDefaultExcludes: false
  }
}

def kibanaIntakeSteps(){
  withEnvWrapper() {
    //unstash 'source'
    //unstash 'cache'
    checkoutSteps()
    dir("${BASE_DIR}"){
      sh '''#!/bin/bash
      set -euxo pipefail
      PATH=${PATH}:$(yarn bin)
      grunt jenkins:unit --from=source --dev || echo -e "\033[31;49mTests FAILED\033[0m"
      '''
    }
  }
}

def kibanaGroupSteps(){
  withEnvWrapper() {
    //unstash 'source'
    //unstash 'cache'
    unstash 'build-oss'
    checkoutSteps()
    dir("${BASE_DIR}"){
      script {
        def parallelSteps = [:]
        def groups = (1..12)
        
        parallelSteps['ensureAllTestsInCiGroup'] = {sh '''#!/bin/bash
        set -euxo pipefail
        PATH=${PATH}:$(yarn bin)
        
        grunt functionalTests:ensureAllTestsInCiGroup || echo -e "\033[31;49mTests FAILED\033[0m"
        '''}
        
        parallelSteps['pluginFunctionalTestsRelease'] = {sh '''#!/bin/bash
        set -euxo pipefail
        PATH=${PATH}:$(yarn bin)
        
        grunt run:pluginFunctionalTestsRelease --from=source || echo -e "\033[31;49mTests FAILED\033[0m"
        '''}
        
        groups.each{ group ->
          parallelSteps["functionalTests_ciGroup${group}"] ={sh """#!/bin/bash
          set -euxo pipefail
          PATH=\${PATH}:\$(yarn bin)
          
          grunt "run:functionalTests_ciGroup${group}" --from=source || echo -e "\033[31;49mTests FAILED\033[0m"
          """}
        }
        parallel(parallelSteps)
      }
    }
  }
}

def xPackIntakeSteps(){
  withEnvWrapper() {
    //unstash 'source'
    //unstash 'cache'
    checkoutSteps()
    dir("${XPACK_DIR}"){
      script {
        def parallelSteps = [:]
        
        parallelSteps['Mocha tests'] = {sh '''#!/bin/bash
        set -euxo pipefail
        PATH=${PATH}:$(yarn bin)
        yarn test'''}
        parallelSteps['Jest tests'] = {sh '''#!/bin/bash
        set -euxo pipefail
        node scripts/jest --ci --no-cache --verbose'''}
        parallel(parallelSteps)
      }
    }
  }
}

def xPackGroupSteps(){
  withEnvWrapper() {
    //unstash 'source'
    //unstash 'cache'
    unstash 'build-no-oss'
    checkoutSteps()
    dir("${XPACK_DIR}"){
      script {
        def parallelSteps = [:]
        def groups = (1..6)
        def funTestGroups = (1..12)
        
        groups.each{ group ->
          parallelSteps["ciGroup${group}"] = {sh """#!/bin/bash
          set -euxo pipefail
          PATH=\${PATH}:\$(yarn bin)
          node scripts/functional_tests --assert-none-excluded --include-tag "ciGroup${group}"
          """}
        }
        funTestGroups.each{ group ->
          parallelSteps["functional and api tests ciGroup${group}"] = {sh """#!/bin/bash
          set -euxo pipefail
          PATH=\${PATH}:\$(yarn bin)
          node scripts/functional_tests --debug --bail --kibana-install-dir "${INSTALL_DIR}" --include-tag "ciGroup${group}"
          """}
        }
        parallel(parallelSteps)
      }
    }
  }
}
