NAME = 'it/log'
DSL = '''pipeline {
  agent any
  stages {
    stage('log with info') {
      steps { log(level: 'INFO', text: 'message') }
    }
    stage('log with debug') {
      stages {
        stage('debug disabled') {
          steps { log(level: 'DEBUG', text: 'message') }
        }
        stage('debug enabled') {
          environment { PIPELINE_LOG_LEVEL = 'DEBUG' }
          steps { log(level: 'DEBUG', text: 'message') }
        }
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

queue(NAME)
