NAME = 'it/dockerLogin'
DSL = '''pipeline {
  agent none
  environment {
    DOCKER_REGISTRY = 'docker.elastic.co'
    DOCKER_SECRET = 'secret/apm-team/ci/docker-registry/prod'
  }
  stages {
    stage('linux') {
      agent { label 'linux && immutable' }
      steps {
        dockerLogin(secret: "${DOCKER_SECRET}", registry: "${DOCKER_REGISTRY}")
      }
    }
    stage('windows') {
      agent { label 'windows-immutable' }
      steps {
        dockerLogin(secret: "${DOCKER_SECRET}", registry: "${DOCKER_REGISTRY}")
      }
    }
  }
}'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
