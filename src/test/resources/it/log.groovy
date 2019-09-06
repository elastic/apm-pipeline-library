pipeline {
  agent any
  stages {
    stage('log with info') {
      steps { log(level: 'INFO', text: 'info message') }
    }
    stage('debug enabled') {
      environment { PIPELINE_LOG_LEVEL = 'DEBUG' }
      steps { log(level: 'DEBUG', text: 'debug message') }
    }
  }
}
