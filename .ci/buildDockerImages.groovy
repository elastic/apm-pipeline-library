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

import groovy.transform.Field

@Library('apm@current') _

@Field def results = [:]

pipeline {
  agent { label 'ubuntu-18 && immutable && docker' }
  environment {
    BASE_DIR="src"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    PIPELINE_LOG_LEVEL='INFO'
    DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
  }
  options {
    timeout(time: 3, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    issueCommentTrigger('(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'registry', defaultValue: "docker.elastic.co", description: "")
    string(name: 'tag_prefix', defaultValue: "observability-ci", description: "")
    string(name: 'secret', defaultValue: "secret/apm-team/ci/docker-registry/prod", description: "")
    booleanParam(name: 'apm_integration_testing', defaultValue: "false", description: "")
    booleanParam(name: 'apm_proxy', defaultValue: "false", description: "APM proxy [https://github.com/elastic/observability-dev/tree/master/tools/apm-proxy]")
    booleanParam(name: 'apm_server', defaultValue: "false", description: "")
    booleanParam(name: 'flakey', defaultValue: "false", description: "Flake detection app")
    booleanParam(name: 'heartbeat', defaultValue: "false", description: "Heartbeat to monitor Jenkins jobs")
    booleanParam(name: 'helm_kubectl', defaultValue: "false", description: "")
    booleanParam(name: 'integrations_reporter', defaultValue: "false", description: "Integrations reporter [https://github.com/elastic/observability-dev/tree/master/apps/automation/integrations/reporter]")
    booleanParam(name: 'nodejs', defaultValue: 'false', description: '')
    booleanParam(name: 'opbot', defaultValue: "false", description: "")
    booleanParam(name: 'oracle_instant_client', defaultValue: "false", description: "")
    booleanParam(name: 'picklesdoc', defaultValue: "false", description: "Pickles Doc generator")
    booleanParam(name: 'python', defaultValue: "false", description: "")
    booleanParam(name: 'ruby', defaultValue: 'false', description: '')
    booleanParam(name: 'rum', defaultValue: 'false', description: '')
    booleanParam(name: 'testPlans', defaultValue: "false", description: "Test Plans app")
    booleanParam(name: 'weblogic', defaultValue: "false", description: "")
    booleanParam(name: 'load_orch', defaultValue: "false", description: "Load testing orchestrator [https://github.com/elastic/observability-dev/tree/master/apps/automation/bandstand]")
  }
  stages {
    stage('Build agent Python images'){
      options {
        skipDefaultCheckout()
        warnError('Build agent Python images failed')
      }
      when{
        beforeAgent true
        expression { return params.python }
      }
      steps {
        deleteDir()
        dir('apm-agent-python'){
          git 'https://github.com/elastic/apm-agent-python.git'
          script {
            def pythonVersions = readYaml(file: '.ci/.jenkins_python.yml')['PYTHON_VERSION']
            def tasks = [:]
            pythonVersions.each { pythonIn ->
              def pythonVersion = pythonIn.replaceFirst("-",":")
              tasks["${pythonVersion}"] = {
                node('ubuntu-18 && immutable && docker'){
                  dockerLoginElasticRegistry()
                  buildDockerImage(
                    repo: 'https://github.com/elastic/apm-agent-python.git',
                    tag: 'apm-agent-python',
                    version: "${pythonIn}",
                    folder: "tests",
                    options: "--build-arg PYTHON_IMAGE=${pythonVersion}",
                    push: true)
                }
              }
            }
            parallel(tasks)
          }
        }
      }
    }
    stage('Build agent Node.js images'){
      options {
        skipDefaultCheckout()
        warnError('Build agent Node.js images failed')
      }
      when{
        beforeAgent true
        expression { return params.nodejs }
      }
      steps {
        dir('apm-agent-nodejs'){
          git 'https://github.com/elastic/apm-agent-nodejs.git'
          script {
            def nodeVersions = readYaml(file: '.ci/.jenkins_nodejs.yml')['NODEJS_VERSION']
            def tasks = [:]
            nodeVersions.each { version ->
              // Versions are double quoted
              def nodejsVersion = version.replaceFirst('"', '')
              tasks["${version}"] = {
                node('ubuntu-18 && immutable && docker'){
                  dockerLoginElasticRegistry()
                  buildDockerImage(
                    repo: 'https://github.com/elastic/apm-agent-nodejs.git',
                    tag: 'apm-agent-nodejs',
                    version: "${nodejsVersion}",
                    folder: '.ci/docker/node-container',
                    options: "--build-arg NODE_VERSION='${nodejsVersion}'",
                    push: true)
                }
              }
            }
            parallel(tasks)
          }
        }
      }
    }
    stage('Build agent Ruby images'){
      options {
        skipDefaultCheckout()
        warnError('Build agent Ruby images failed')
      }
      environment {
        TAG_CACHE = "${params.registry}/${params.tag_prefix}"
      }
      when{
        beforeAgent true
        expression { return params.ruby }
      }
      steps {
        dir('apm-agent-ruby'){
          git 'https://github.com/elastic/apm-agent-ruby.git'
          dir('.ci/docker/jruby'){
            sh(label: 'build docker images', script: "./run.sh --action build --registry ${TAG_CACHE}")
            sh(label: 'test docker images', script: "./run.sh --action test --registry ${TAG_CACHE}")
            dockerLoginElasticRegistry()
            sh(label: 'push docker images', script: "./run.sh --action push --registry ${TAG_CACHE}")
            archiveArtifacts '*.log'
          }
          script {
            def rubyVersions = readYaml(file: '.ci/.jenkins_ruby.yml')['RUBY_VERSION']
            def tasks = [:]
            // The ones with the observability-ci tag are already built at the very end
            // of this pipeline.
            rubyVersions.findAll { element -> !element.contains('observability-ci') }.each { version ->
              def rubyVersion = version.replaceFirst(":","-")
              tasks["${rubyVersion}"] = {
                node('ubuntu-18 && immutable && docker'){
                  dockerLoginElasticRegistry()
                  buildDockerImage(
                    repo: 'https://github.com/elastic/apm-agent-ruby.git',
                    tag: 'apm-agent-ruby',
                    version: "${rubyVersion}",
                    folder: 'spec',
                    options: "--build-arg RUBY_IMAGE='${version}'",
                    push: true)
                }
              }
            }
            parallel(tasks)
          }
        }
      }
    }
    stage('Build agent RUM images'){
      options {
        skipDefaultCheckout()
        warnError('Build agent RUM images failed')
      }
      when{
        beforeAgent true
        expression { return params.rum }
      }
      steps {
        dir('apm-agent-rum-js'){
          git 'https://github.com/elastic/apm-agent-rum-js.git'
          script {
            def imagesConfiguration = readYaml(file: '.ci/.jenkins_rum.yml')
            def libraries = imagesConfiguration['TEST_LIBRARIES']
            def nodejsVersion = readFile("./dev-utils/.node-version").trim()
            def tasks = [:]
            libraries.each { library ->
              tasks["${library}-${nodejsVersion}"] = {
                node('ubuntu-18 && immutable && docker'){
                  dockerLoginElasticRegistry()
                  buildDockerImage(
                    repo: 'https://github.com/elastic/apm-agent-rum-js.git',
                    tag: "node-${library}",
                    version: "${nodejsVersion}",
                    folder: ".ci/docker/node-${library}",
                    options: "--build-arg NODEJS_VERSION='${nodejsVersion}'",
                    push: true)
                }
              }
            }
            parallel(tasks)
          }
        }
      }
    }
    stage('Build APM Proxy'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.apm_proxy}
      }
      steps{
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-dev',
          prepareWith: {dir("spoa-mirror"){git credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', url: "https://github.com/haproxytech/spoa-mirror.git"}},
          tag: "apm-proxy",
          version: "latest",
          folder: "tools/apm_proxy/frontend",
          push: true
        )
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-dev',
          tag: "apm-proxy-be",
          version: "latest",
          folder: "tools/apm_proxy/backend",
          push: true
        )
      }
    }
    stage('Build Apm Server test Docker images'){
      options {
        skipDefaultCheckout()
        warnError('Build Apm Server Docker images failed')
      }
      when{
        beforeAgent true
        expression { return params.apm_server }
      }
      steps {
        deleteDir()
        checkout scm
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/apm-server.git',
          tag: "apm-server",
          version: "daily",
          push: true
        )
        dir("apm-server-images"){
          git('https://github.com/elastic/apm-server.git')
          sh(label: 'Test Docker containers', script: 'make -C .ci/docker all-tests')
          retry(3){
            sh(label: 'Push Docker images', script: 'make -C .ci/docker all-push')
          }
          sh(label: 'clean Docker images', script: 'docker rmi --force $(docker images --filter=reference="docker.elastic.co/observability-ci/*:*" -q)')
        }
      }
      post {
        always {
          junit(allowEmptyResults: true,
            keepLongStdio: true,
            testResults: "${BASE_DIR}/**/junit-*.xml")
        }
      }
    }
    stage('Build Curator image'){
      options {
        skipDefaultCheckout()
        warnError('Build Curator image failed')
      }
      when{
        beforeAgent true
        expression { return params.python }
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/curator.git',
          tag: "curator",
          version: "daily",
          push: true)
      }
    }
    stage('Build flakey'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.flakey}
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-dev',
          tag: 'flakey',
          version: 'latest',
          push: true,
          folder: "apps/automation/jenkins-toolbox")
      }
    }
    stage('Build integrations test reporter'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.integrations_reporter}
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-dev',
          tag: 'integrations-test-reporter',
          version: 'latest',
          push: true,
          folder: "/apps/automation/integrations/reporter")
      }
    }
    stage('Build Heartbeat'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.heartbeat}
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        git credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', url: 'https://github.com/elastic/observability-robots'
        dir("apps/beats/heartbeat"){
          script{
            sh("pip3 install pyyaml")
            sh("python3 ./generate_heartbeat_configs.py")
            def writeClosure = {sh(script: "cp -R ${WORKSPACE}/apps/beats/heartbeat/configs configs/ && cp ${WORKSPACE}/apps/beats/heartbeat/heartbeat.yml heartbeat.yml")}
            buildDockerImage(
              repo: "https://github.com/elastic/observability-robots",
              tag: 'obs-jenkins-heartbeat',
              version: 'latest',
              push: true,
              prepareWith: writeClosure,
              folder: "apps/beats/heartbeat"
            )
          }
        }
      }
    }
    stage('Build helm-kubernetes Docker hub image'){
      options {
        skipDefaultCheckout()
        warnError('Build helm-kubernetes Docker hub image failed')
      }
      when{
        beforeAgent true
        expression { return params.helm_kubectl }
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/dtzar/helm-kubectl.git',
          tag: "helm-kubectl",
          version: "latest",
          push: true)
      }
    }
    stage('Build Integration test Docker images'){
      options {
        skipDefaultCheckout()
        warnError('Build Integration test Docker images failed')
      }
      when{
        beforeAgent true
        expression { return params.apm_integration_testing }
      }
      steps {
        deleteDir()
        checkout scm
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/apm-integration-testing.git',
          tag: "apm-integration-testing",
          version: "daily",
          push: true
        )
        dir("integration-testing-images"){
          git('https://github.com/elastic/apm-integration-testing.git')
          sh(label: 'Test Docker containers', script: 'make -C docker all-tests')
          retry(3){
            sh(label: 'Push Docker images', script: 'make -C docker all-push')
          }
          sh(label: 'clean Docker images', script: 'docker rmi --force $(docker images --filter=reference="docker.elastic.co/observability-ci/*:*" -q)')
        }
      }
      post {
        always {
          junit(allowEmptyResults: true,
            keepLongStdio: true,
            testResults: "${BASE_DIR}/**/junit-*.xml")
        }
      }
    }
    stage('Build load-test orchestrator'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.load_orch}
      }
      steps{
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-dev',
          tag: "bandstand",
          version: "latest",
          folder: "apps/automation/bandstand",
          push: true
        )
      }
    }
    stage('Build opbot'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.opbot }
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        dir("opbot-latest"){
          script {
            def creds = getVaultSecret('secret/k8s/elastic-apps/apm/opbot-google-creds')
            def writeClosure = {writeFile(file: 'credentials.json', text: creds.data.value)}
            buildDockerImage(
              repo: 'https://github.com/elastic/opbot.git',
              tag: "opbot",
              version: "latest",
              prepareWith: writeClosure,
              push: true)
          }
        }
      }
      post {
        cleanup {
          deleteDir()
        }
      }
    }
    stage('Cache Oracle Instant Client Docker Image'){
      environment {
        IMAGE_TAG = "store/oracle/database-instantclient:12.2.0.1"
        TAG_CACHE = "${params.registry}/${params.tag_prefix}/database-instantclient:12.2.0.1"
        HOME = "${env.WORKSPACE}"
      }
      options {
        warnError('Cache Oracle Instant Client Docker Image failed')
      }
      when{
        beforeAgent true
        expression { return params.oracle_instant_client }
      }
      steps {
        deleteDir()
        pushDockerImageFromStore("${IMAGE_TAG}", "${TAG_CACHE}")
      }
    }
    stage('Build pickles'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.picklesdoc}
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-robots.git',
          tag: 'picklesdoc',
          buildCommand: 'make build',
          pushCommand: 'make push',
          push: true,
          folder: "apps/pickles")
      }
    }
    stage('Build test-plans'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.testPlans}
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/observability-robots.git',
          tag: 'test-plans',
          buildCommand: 'make build',
          pushCommand: 'make push',
          push: true,
          folder: "apps/test-plans")
      }
    }
    stage('Cache Weblogic Docker Image'){
      environment {
        IMAGE_TAG = "store/oracle/weblogic:12.2.1.3-dev"
        TAG_CACHE = "${params.registry}/${params.tag_prefix}/weblogic:12.2.1.3-dev"
        HOME = "${env.WORKSPACE}"
      }
      options {
        warnError('Cache Weblogic Docker Image failed')
      }
      when{
        beforeAgent true
        expression { return params.weblogic }
      }
      steps {
        deleteDir()
        pushDockerImageFromStore("${IMAGE_TAG}", "${TAG_CACHE}")
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def dockerLoginElasticRegistry(){
  if(params.secret != null && "${params.secret}" != ""){
     dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
  }
}

def buildDockerImage(args){
  String repo = args.containsKey('repo') ? args.repo : error("Repository not valid")
  String tag = args.containsKey('tag') ? args.tag : error("Tag not valid")
  String version = args.containsKey('version') ? args.version : "latest"
  String folder = args.containsKey('folder') ? args.folder : "."
  String buildCommand = args.containsKey('buildCommand') ? args.buildCommand : ""
  String pushCommand = args.containsKey('pushCommand') ? args.pushCommand : ""
  def env = args.containsKey('env') ? args.env : []
  String options = args.containsKey('options') ? args.options : ""
  boolean push = args.containsKey('push') ? args.push : false
  def prepareWith = args.containsKey('prepareWith') ? args.prepareWith : {}

  def image = "${params.registry}"
  if(params.tag_prefix != null && params.tag_prefix != ""){
    image += "/${params.tag_prefix}"
  }
  image += "/${tag}:${version}"
  dir("${tag}-${version}"){
    git credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', url: "${repo}"
    dir("${folder}"){
      withEnv(env){
        prepareWith()
        if (buildCommand.equals("")) {
          sh(label: "build docker image", script: "docker build --force-rm ${options} -t ${image} .")
        } else {
          sh(label: "custom build docker image", script: "${buildCommand}")
        }

        if(push){
          retry(3){
            if (pushCommand.equals("")) {
              sh(label: "push docker image", script: "docker push ${image}")
            } else {
              sh(label: "custom push docker image", script: "${pushCommand}")
            }
          }
        }
        sh(label: "clean docker image", script: "docker rmi --force ${image}")
      }
    }
  }
}

def pushDockerImageFromStore(imageTag, cacheTag){
  dockerLogin(secret: "${DOCKERHUB_SECRET}", registry: 'docker.io')
  sh(label: 'pull Docker image', script: "docker pull ${imageTag}")
  dockerLoginElasticRegistry()
  sh(label: 're-tag Docker image', script: "docker tag ${imageTag} ${cacheTag}")
  sh(label: "push Docker image to ${cacheTag}", script: "docker push ${cacheTag}")
  sh(label: 'clean Docker images', script: "docker rmi --force ${imageTag}")
}
