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
  agent { label 'immutable && docker' }
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
    booleanParam(name: 'python', defaultValue: "false", description: "")
    booleanParam(name: 'ruby', defaultValue: 'false', description: '')
    booleanParam(name: 'weblogic', defaultValue: "false", description: "")
    booleanParam(name: 'oracle_instant_client', defaultValue: "false", description: "")
    booleanParam(name: 'apm_integration_testing', defaultValue: "false", description: "")
    booleanParam(name: 'helm_kubectl', defaultValue: "false", description: "")
    booleanParam(name: 'jruby', defaultValue: "false", description: "")
  }
  stages {
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
            dockerLoginElasticRegistry()
            def pythonVersions = readYaml(file: '.ci/.jenkins_python.yml')['PYTHON_VERSION']
            def tasks = [:]
            pythonVersions.each { pythonIn ->
              def pythonVersion = pythonIn.replaceFirst("-",":")
              tasks["${pythonVersion}"] = {
                buildDockerImage(
                  repo: 'https://github.com/elastic/apm-agent-python.git',
                  tag: 'apm-agent-python',
                  version: "${pythonIn}",
                  folder: "tests",
                  options: "--build-arg PYTHON_IMAGE=${pythonVersion}",
                  push: true)
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
      when{
        beforeAgent true
        expression { return params.ruby }
      }
      steps {
        dir('apm-agent-ruby'){
          git 'https://github.com/elastic/apm-agent-ruby.git'
          script {
            dockerLoginElasticRegistry()
            def rubyVersions = readYaml(file: '.ci/.jenkins_ruby.yml')['RUBY_VERSION']
            def tasks = [:]
            // The ones with the observability-ci tag are already built at the very end
            // of this pipeline.
            rubyVersions.findAll { it -> !it.contains('observability-ci') }.each { version ->
              def rubyVersion = version.replaceFirst("-",":")
              tasks["${rubyVersion}"] = {
                buildDockerImage(
                  repo: 'https://github.com/elastic/apm-agent-ruby.git',
                  tag: 'apm-agent-ruby',
                  version: "${version}",
                  folder: 'spec',
                  options: "--build-arg RUBY_IMAGE=${rubyVersion}",
                  push: true)
              }
            }
            parallel(tasks)
          }
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
          sh(label: 'Push Docker images', script: 'make -C docker all-push')
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
    stage('Build Apm Server test Docker images'){
      options {
        skipDefaultCheckout()
        warnError('Build Apm Server Docker images failed')
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
          repo: 'https://github.com/elastic/apm-server.git',
          tag: "apm-server",
          version: "daily",
          push: true
        )
        dir("apm-server-images"){
          git('https://github.com/elastic/apm-server.git')
          sh(label: 'Test Docker containers', script: 'make -C .ci/docker all-tests')
          sh(label: 'Push Docker images', script: 'make -C .ci/docker all-push')
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
    stage('Build JRuby-jdk Docker images'){
      options {
        skipDefaultCheckout()
        warnError('Build JRuby-jdk Docker images failed')
      }
      environment {
        TAG_CACHE = "${params.registry}/${params.tag_prefix}"
      }
      when{
        beforeAgent true
        expression { return params.jruby }
      }
      steps {
        deleteDir()
        git url: 'https://github.com/elastic/docker-jruby', branch: 'versions'
        sh(label: 'build docker images', script: "./run.sh --action build --registry ${TAG_CACHE} --exclude 1.7")
        sh(label: 'test docker images', script: "./run.sh --action test --registry ${TAG_CACHE} --exclude 1.7")
        dockerLoginElasticRegistry()
        sh(label: 'push docker images', script: "./run.sh --action push --registry ${TAG_CACHE} --exclude 1.7")
        archiveArtifacts '*.log'
      }
    }
    stage('Build opbot'){
      options {
        skipDefaultCheckout()
      }
      when{
        beforeAgent true
        expression { return params.jruby }
      }
      steps {
        deleteDir()
        dockerLoginElasticRegistry()
        buildDockerImage(
          repo: 'https://github.com/elastic/opbot.git',
          tag: "opbot",
          version: "latest",
          push: true)
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
  def env = args.containsKey('env') ? args.env : []
  String options = args.containsKey('options') ? args.options : ""
  boolean push = args.containsKey('push') ? args.push : false

  def image = "${params.registry}"
  if(params.tag_prefix != null && params.tag_prefix != ""){
    image += "/${params.tag_prefix}"
  }
  image += "/${tag}:${version}"
  dir("${tag}-${version}"){
    git credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', url: "${repo}"
    dir("${folder}"){
      withEnv(env){
        sh(label: "build docker image", script: "docker build ${options} -t ${image} .")
        if(push){
          sh(label: "push docker image", script: "docker push ${image}")
        }
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
}
