NAME = 'it/cloud'
DSL = '''
@Library('cloud@master') _
pipeline {
  agent none
  stages {
    stage('linux') {
      steps {
        retryWithSleep(1) {
          echo 'hi'
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
