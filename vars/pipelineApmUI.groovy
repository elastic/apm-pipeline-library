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
  env.NODE_DIR="${WORKSPACE}/node/${nodeVersion}"
  env.NODE_BIN="${NODE_DIR}/bin"
  env.PATH="${NODE_BIN}:${BASE_DIR}/node_modules/.bin:${PATH}"
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

def checkoutSteps(){
  sh 'export'
  withEnvWrapper() {
    dir("${BASE_DIR}"){
      script{
        if(!env?.branch_specifier){
          echo "Checkout SCM"
          checkout scm
        } else {
          echo "Checkout ${branch_specifier}"
          checkout([$class: 'GitSCM', branches: [[name: "${branch_specifier}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            submoduleCfg: [],
            userRemoteConfigs: [[credentialsId: "${JOB_GIT_CREDENTIALS}",
            url: "${GIT_URL}"]]])
        }
        env.JOB_GIT_COMMIT = getGitCommitSha()
        env.JOB_GIT_URL = "${GIT_URL}"
        github_enterprise_constructor()
      }
    }
    stash allowEmpty: true, name: 'source', useDefaultExcludes: false
    dir("${BASE_DIR}"){
      script{  
        def packageJson = readJSON(file: 'package.json')
        env.NODE_VERSION = packageJson.engines.node
        env.YARN_VERSION = packageJson.engines.yarn
        installNodeJs("${NODE_VERSION}", ["yarn@${YARN_VERSION}"])
        sh """#!/bin/bash
        set -euxo pipefail
        yarn kbn bootstrap
        """
      }
      sh 'pwd'
    }
    sh """
    ls -la ${BASE_DIR}/node_modules,
    ls -la ${WORKSPACE}/node
    """
    stash allowEmpty: true, name: 'cache', includes: "${BASE_DIR}/node_modules/**,${WORKSPACE}/node/**", useDefaultExcludes: false
    dir("${ES_BASE_DIR}"){
      /** TODO grab the correct elasticsearch branch */
      checkout([$class: 'GitSCM', branches: [[name: "master"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [],
        submoduleCfg: [],
        userRemoteConfigs: [[credentialsId: "${JOB_GIT_CREDENTIALS}",
        url: "${ES_GIT_URL}"]]])
    }
    stash allowEmpty: true, name: 'es', includes: "${ES_BASE_DIR}/**", useDefaultExcludes: false
  }
}

def buildOSSSteps(){
  withEnvWrapper() {
    unstash 'source'
    unstash 'cache'
    nodeEnviromentVars("${NODE_VERSION}")
    dir("${BASE_DIR}"){
      sh '''#!/bin/bash
      set -euxo pipefail
      node scripts/build --debug --oss --skip-node-download --skip-archives --skip-os-packages
      '''
    }
    stash allowEmpty: true, name: 'build-oss', includes: "${BASE_DIR}/build", useDefaultExcludes: false
  }
}

def buildNoOSSSteps(){
  withEnvWrapper() {
    unstash 'source'
    unstash 'cache'
    nodeEnviromentVars("${NODE_VERSION}")
    dir("${BASE_DIR}"){
      sh '''#!/bin/bash
      set -euxo pipefail
      node scripts/build --debug --no-oss --skip-node-download --skip-os-packages
      '''
      sh '''#!/bin/bash
      set -euxo pipefail
      linuxBuild="$(find "./target" -name 'kibana-*-linux-x86_64.tar.gz')"
      installDir="${WORKSPACE}/install/kibana"
      mkdir -p "${installDir}"
      tar -xzf "${linuxBuild}" -C "${installDir}" --strip=1
      '''
    }
    stash allowEmpty: true, name: 'kibana-bin', includes: "${WORKSPACE}/install/kibana", useDefaultExcludes: false
    stash allowEmpty: true, name: 'build-no-oss', includes: "${BASE_DIR}/build", useDefaultExcludes: false
  }
}

def kibanaIntakeSteps(){
  withEnvWrapper() {
    unstash 'source'
    unstash 'cache'
    nodeEnviromentVars("${NODE_VERSION}")
    dir("${BASE_DIR}"){
      sh '''#!/bin/bash
      set -euxo pipefail
      pwd
      export
      "$(yarn bin)/grunt" jenkins:unit --from=source --dev;
      '''
    }
  }
}

def kibanaGroupSteps(){
  withEnvWrapper() {
    unstash 'source'
    unstash 'cache'
    unstash 'build-oss'
    nodeEnviromentVars("${NODE_VERSION}")
    dir("${BASE_DIR}"){
      script {
        def parallelSteps = Map [:]
        def groups = (1..12)
        
        parallelSteps['ensureAllTestsInCiGroup'] = {sh '''#!/bin/bash
        set -euxo pipefail
        "$(yarn bin)/grunt" functionalTests:ensureAllTestsInCiGroup;
        '''}
        
        parallelSteps['pluginFunctionalTestsRelease'] = {sh '''#!/bin/bash
        set -euxo pipefail
        "$(yarn bin)/grunt" run:pluginFunctionalTestsRelease --from=source;
        '''}
        
        groups.each{ group ->
          parallelSteps["functionalTests_ciGroup${group}"] ={sh """#!/bin/bash
          set -euxo pipefail
          "\$(yarn bin)/grunt" "run:functionalTests_ciGroup${group}" --from=source;
          """}
        }
        parallel(parallelSteps)
      }
    }
  }
}

def xPackIntakeSteps(){
  withEnvWrapper() {
    unstash 'source'
    unstash 'cache'
    nodeEnviromentVars("${NODE_VERSION}")
    dir("${XPACK_DIR}"){
      script {
        def parallelSteps = Map [:]
        
        parallelSteps['Mocha tests'] = {sh '''#!/bin/bash
        set -euxo pipefail
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
    unstash 'source'
    unstash 'cache'
    unstash 'build-no-oss'
    nodeEnviromentVars("${NODE_VERSION}")
    dir("${XPACK_DIR}"){
      script {
        def parallelSteps = Map [:]
        def groups = (1..6)
        def funTestGroups = (1..12)
        
        groups.each{ group ->
          parallelSteps["ciGroup${group}"] = {sh """#!/bin/bash
          set -euxo pipefail
          node scripts/functional_tests --assert-none-excluded --include-tag "ciGroup${group}"
          """}
        }
        funTestGroups.each{ group ->
          parallelSteps["functional and api tests ciGroup${group}"] = {sh """#!/bin/bash
          set -euxo pipefail
          node scripts/functional_tests --debug --bail --kibana-install-dir "${INSTALL_DIR}" --include-tag "ciGroup${group}"
          """}
        }
        parallel(parallelSteps)
      }
    }
  }
}