pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        sh 'ls -ltra'
      }
    }
  }
}
