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
    DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '2', artifactNumToKeepStr: '2', daysToKeepStr: '30'))
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
    string(name: 'secret', defaultValue: "secret/apm-team/ci/elastic-observability-docker-elastic-co", description: "")
    booleanParam(name: 'python', defaultValue: "false", description: "")
    booleanParam(name: 'weblogic', defaultValue: "false", description: "")
    booleanParam(name: 'apm_integration_testing', defaultValue: "false", description: "")
  }
  stages {
    stage('Cache Weblogic Docker Image'){
      agent { label 'immutable && docker' }
      environment {
        TAG_CACHE = "${params.registry}/${params.tag_prefix}/weblogic:12.2.1.3-dev"
        HOME = "${env.WORKSPACE}"
      }
      when{
        beforeAgent true
        expression { return params.weblogic }
      }
      steps {
        script{
          dockerLogin(secret: "${DOCKERHUB_SECRET}", registry: 'docker.io')
          sh(label: 'pull Docker image', script: "docker pull store/oracle/weblogic:12.2.1.3-dev")

          if(params.secret != null && "${params.secret}" != ""){
             dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
          }
          sh(label: 're-tag Docker image', script: "docker tag store/oracle/weblogic:12.2.1.3-dev ${TAG_CACHE}")
          sh(label: "push Docker image to ${TAG_CACHE}", script: "docker push ${TAG_CACHE}")
        }
      }
    }
    stage('Build agent Python images'){
      agent { label 'immutable && docker' }
      options { skipDefaultCheckout() }
      when{
        beforeAgent true
        expression { return params.python }
      }
      steps {
        dir('apm-agent-python'){
          git 'https://github.com/elastic/apm-agent-python.git'
          script {
            if(params.secret != null && "${params.secret}" != ""){
               dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
            }
            def pythonVersions = readYaml(file: 'tests/.jenkins_python.yml')['PYTHON_VERSION']
            def tasks = [:]
            pythonVersions.each { pythonIn ->
              def pythonVersion = pythonIn.replace("-",":")
              tasks["${pythonVersion}"] = {
                buildDockerImage(
                  repo: 'https://github.com/elastic/apm-agent-python.git',
                  tag: "apm-agent-python-test",
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
    stage('Build Curator image'){
      agent { label 'immutable && docker' }
      options { skipDefaultCheckout() }
      when{
        beforeAgent true
        expression { return params.python }
      }
      steps {
        buildDockerImage(
          repo: 'https://github.com/elastic/curator.git',
          tag: "curator",
          version: "daily",
          folder: "curator",
          push: true)
      }
    }
    stage('Build Integration test Docker images'){
      agent { label 'immutable && docker' }
      options { skipDefaultCheckout() }
      when{
        beforeAgent true
        expression { return params.apm_integration_testing }
      }
      steps {
        buildDockerImage(
          repo: 'https://github.com/elastic/apm-integration-testing.git',
          tag: "apm-integration-testing",
          version: "daily",
          push: true)
      }
    }
  }
  post {
    always {
      node('master'){
        log(level: "INFO", text: "=====SUMARY=====")
        script {
          results.each{ k, v ->
            level = "INFO"
            if(!v){
              level = "ERROR"
              currentBuild.result = "FAILURE"
            }
            log(level: "INFO", text: "${k}")
          }
        }
      }
    }
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
