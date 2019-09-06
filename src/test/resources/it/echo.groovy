pipeline {
  agent any
  stages {
    stage('echo') {
      steps {
        echo 'hi'
      }
    }
  }
}
