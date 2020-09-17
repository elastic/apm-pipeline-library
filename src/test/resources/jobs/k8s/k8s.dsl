NAME = 'it/k8s/k8s'
DSL = '''pipeline {
  agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: some-label-value
spec:
  containers:
  - name: maven
    image: maven:alpine
    command:
    - cat
    tty: true
"""
      defaultContainer 'maven'
    }
  }
  stages {
    stage('Run maven') {
      steps {
        sh 'mvn -version'
        container('jnlp') {
          sh 'git --version'
        }
        container('apm-pipeline-library') {
          sh 'git --version'
          sh 'java -version'
        }
      }
    }
  }
}
'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
