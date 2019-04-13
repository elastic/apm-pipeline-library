#!/usr/bin/env groovy
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
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    cron 'H H(3-4) * * 1-5'
    issueCommentTrigger('.*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'registry', defaultValue: "docker.elastic.co", description: "")
    string(name: 'tag_prefix', defaultValue: "observability-ci", description: "")
    string(name: 'version', defaultValue: "daily", description: "")
    string(name: 'secret', defaultValue: "secret/apm-team/ci/elastic-observability-docker-elastic-co", description: "")
    booleanParam(name: 'opbeans', defaultValue: "false", description: "")
  }
  stages {
    stage('Build Opbeans images'){
      when{
        beforeAgent true
        expression { return params.opbeans }
      }
      parallel {
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
    success {
      echoColor(text: '[SUCCESS]', colorfg: 'green', colorbg: 'default')
    }
    aborted {
      echoColor(text: '[ABORTED]', colorfg: 'magenta', colorbg: 'default')
    }
    failure {
      echoColor(text: '[FAILURE]', colorfg: 'red', colorbg: 'default')
      node('master'){
        step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "${NOTIFY_TO}", sendToIndividuals: false])
      }
    }
    unstable {
      echoColor(text: '[UNSTABLE]', colorfg: 'yellow', colorbg: 'default')
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
        sh(label: "build docker image", script: "docker build ${options} -t ${image} .")
        if(push){
          sh(label: "push docker image", script: "docker push ${image}")
        }
      }
    }
  }
}
