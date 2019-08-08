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
  agent none
  environment {
    BASE_DIR="src"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    PIPELINE_LOG_LEVEL='INFO'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    cron 'H H(3-4) * * 1-5'
    issueCommentTrigger('(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'registry', defaultValue: "docker.elastic.co", description: "")
    string(name: 'tag_prefix', defaultValue: "observability-ci", description: "")
    string(name: 'version', defaultValue: "daily", description: "")
    string(name: 'secret', defaultValue: "secret/apm-team/ci/docker-registry/prod", description: "")
    booleanParam(name: 'opbeans', defaultValue: "false", description: "")
  }
  stages {
    stage('Build Opbeans images'){
      when{
        beforeAgent true
        expression { return params.opbeans }
      }
      parallel {
        stage('Opbeans-dotnet') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          /** FIXME disable until 7.4 is not released */
          when {
            expression { return false }
          }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-dotnet.git',
              tag: "opbeans-dotnet",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-node') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-node.git',
              tag: "opbeans-node",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-python') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-python.git',
              tag: "opbeans-python",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-frontend') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-frontend.git',
              tag: "opbeans-frontend",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-java') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-java.git',
              tag: "opbeans-java",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-go') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-go.git',
              tag: "opbeans-go",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-loadgen') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-loadgen.git',
              tag: "opbeans-loadgen",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-flask') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          /** FIXME disable until it is fully implemented: https://github.com/elastic/opbeans-flask/pull/5 */
          when {
            expression { return false }
          }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-flask.git',
              tag: "opbeans-flask",
              version: "${params.version}",
              push: true)
          }
        }
        stage('Opbeans-ruby') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-ruby.git',
              tag: "opbeans-ruby",
              version: "${params.version}",
              push: true)
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

def buildDockerImage(args){
  String repo = args.containsKey('repo') ? args.repo : error("Repository not valid")
  String tag = args.containsKey('tag') ? args.tag : error("Tag not valid")
  String version = args.containsKey('version') ? args.version : "latest"
  String folder = args.containsKey('folder') ? args.folder : "."
  def env = args.containsKey('env') ? args.env : []
  String options = args.containsKey('options') ? args.options : ""
  boolean push = args.containsKey('push') ? args.push : false
  sleep randomNumber(min: 10, max: 30)
  if(params.secret != null && "${params.secret}" != ""){
    dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
  }
  def image = "${params.registry}"
  if(params.tag_prefix != null && params.tag_prefix != ""){
    image += "/${params.tag_prefix}"
  }
  image += "/${tag}:${version}"
  dir("${tag}-${version}"){
    git "${repo}"
    dir("${folder}"){
      withEnv(env){
        retry(3) {
          sleep randomNumber(min: 5, max: 10)
          sh(label: "build docker image", script: "docker build ${options} -t ${image} .")
        }
        if(push){
          sh(label: "push docker image", script: "docker push ${image}")
        }
      }
    }
  }
}
