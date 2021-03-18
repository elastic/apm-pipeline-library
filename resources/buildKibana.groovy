pipeline {
  agent { label 'linux && immutable' }
  stages {
    stage('Docker build for Kibana') {
      steps {
        buildKibanaDockerImage(refspec: 'PR/94867', baseDir: 'kibana')
      }
    }
  }
}
