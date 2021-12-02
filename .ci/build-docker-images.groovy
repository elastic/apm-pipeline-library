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
  agent { label "ubuntu-20"}
  environment {
    BASE_DIR="${params.name}-${params.tag}"
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
    DOCKER_SECRET = "secret/observability-team/ci/docker-registry/prod"
    HOME="${env.WORKSPACE}"
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  /*
    jobDSL parameters see .ci/jobDSL/jobs/apm-ci/apm-shared/docker-images/build-docker-images.groovy
  */
  stages {
    stage('Checkout'){
      options { skipDefaultCheckout() }
      steps {
        dir("${BASE_DIR}"){
          git(url:"${params.repo}",credentialsId:"f6c7695a-671e-4f4f-a331-acdce44ff9ba", branch:"${params.branch_specifier}")
        }
        prepare()
      }
    }
    stage('build') {
      options { skipDefaultCheckout() }
      steps {
        buildDocker()
      }
    }
    stage('test') {
      options { skipDefaultCheckout() }
      steps {
        testDocker()
      }
    }
    stage('push'){
      options { skipDefaultCheckout() }
      when {
        expression {
          return params.push
        }
      }
      steps {
        pushDocker()
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def generateImageName(){
  def image = "${params.registry}"
  if(isNotBlank(params.prefix)){
    image += "/${params.prefix}"
  }
  image += "/${params.name}:${params.tag}"
  return image
}

def buildDocker(){
  def script = isNotBlank(params.docker_build_script) ? params.docker_build_script : "docker build --force-rm ${params.docker_build_opts} -t ${generateImageName()} ."
  dir("${BASE_DIR}"){
    dockerLogin(secret: "${DOCKER_SECRET}", registry: "${params.registry}")
    dir("${params.folder}"){
      retry(3) {
        sh(label: "Build the Docker image", script: script)
      }
    }
  }
}

def testDocker(){
  def script = isNotBlank(params.docker_test_script) ? params.docker_test_script : "echo 'TBD'"
  dir("${BASE_DIR}"){
    dir("${params.folder}"){
      sh(label: 'Test the Docker image', script: script)
    }
  }
}

def pushDocker(){
  def script = isNotBlank(params.docker_push_script) ? params.docker_push_script : "docker push ${generateImageName()}"
  dir("${BASE_DIR}"){
    dir("${params.folder}"){
      retry(3) {
        sh(label: "Push the Docker image", script: script)
      }
    }
  }
}

def isNotBlank(value){
  return value != null && value != "" && "${value}".trim() != ""
}

def prepare(){
  dir("${BASE_DIR}"){
    if(isNotBlank(params.prepare_script)){
      sh(label: "Prepare workspace", script: params.prepare_script)
    }
  }
}
