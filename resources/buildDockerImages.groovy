#!/usr/bin/env groovy

@Library('apm@current') _

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
    string(name: 'tag_prefix', defaultValue: "employees/kuisathaverat", description: "")
  }
  stages {
    stage('Build Opbeans images'){
      parallel {
        stage('Opbeans-node') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-node.git',
              tag: "opbeans-node",
              version: "daily")
          }
        }
        stage('Opbeans-python') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-python.git',
              tag: "opbeans-python",
              version: "daily")
          }
        }
        stage('Opbeans-frontend') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-frontend.git',
              tag: "opbeans-frontend",
              version: "daily")
          }
        }
        stage('Opbeans-java') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-java.git',
              tag: "opbeans-java",
              version: "daily")
          }
        }
        stage('Opbeans-go') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-go.git',
              tag: "opbeans-go",
              version: "daily")
          }
        }
        stage('Opbeans-loadgen') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-loadgen.git',
              tag: "opbeans-loadgen",
              version: "daily")
          }
        }
        stage('Opbeans-flask') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-flask.git',
              tag: "opbeans-flask",
              version: "daily")
          }
        }
        stage('Opbeans-ruby') {
          agent { label 'docker' }
          options { skipDefaultCheckout() }
          steps {
            buildDockerImage(repo: 'https://github.com/elastic/opbeans-ruby.git',
              tag: "opbeans-ruby",
              version: "daily")
          }
        }
      }
    }
    stage('Build agent Python images'){
      agent { label 'docker' }
      options { skipDefaultCheckout() }
      steps {
        echo "NOOP"
        // dir('apm-agent-python'){
        //   git 'https://github.com/elastic/apm-agent-python.git'
        //   script {
        //     def pythonVersions = readYaml(file: 'tests/.jenkins_python.yml')['PYTHON_VERSION']
        //     def tasks = [:]
        //     pythonVersions.each { pythonIn ->
        //       def pythonVersion = pythonIn.replace("-",":")
        //       tasks["${pythonVersion}"] = buildDockerImage(
        //           repo: 'https://github.com/elastic/apm-agent-python.git',
        //           tag: "apm-agent-python-test",
        //           version: "${pythonVersion}",
        //           dir: "tests",
        //           options: "--build-arg PYTHON_IMAGE=${pythonVersion}")
        //     }
        //     parallel(tasks)
        //   }
        // }
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
  def repo = args.containsKey('repo') ? args.repo : error("Repository not valid")
  def tag = args.containsKey('tag') ? args.tag : error("Tag not valid")
  def version = args.containsKey('version') ? args.version : "latest"
  def dir = args.containsKey('dir') ? args.dir : "."
  def env = args.containsKey('env') ? args.env : []
  def options = args.containsKey('options') ? args.options : ""

  try {
    dir("${tag}"){
      git "${repo}"
      dir("${dir}"{
        withEnv(env){
          def image = "${params.registry}/${params.tag_prefix}/${tag}:${version}"
          sh(label: "build docker image", script: "docker build ${options} -t ${image} .")
          sh(label: "push docker image", script: "docker push ${image}")
        }
      }
    }
  } catch (e){
    log(level: "ERROR", text: "${tag} failed: ${e?.getMessage()}")
    currentBuild.result = "UNSTABLE"
  }
}
