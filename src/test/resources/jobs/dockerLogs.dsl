NAME = 'it/dockerLogs'
DSL = '''pipeline {
  agent { label 'linux && immutable' }
  stages {
    stage('Docker run') {
      steps {
        sh 'docker run hello-world'
      }
    }
    stage('DockerLogs') {
      steps {
        dockerLogs(step: 'docker-logs', failNever: true)
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
