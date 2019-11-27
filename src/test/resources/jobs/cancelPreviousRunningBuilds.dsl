NAME = 'it/cancelPreviousRunningBuilds'
DSL = '''pipeline {
  agent any
  stages {
    stage('cancelPreviousRunningBuilds') {
      steps {
        sleep randomNumber(min: 5, max: 30)
        cancelPreviousRunningBuilds
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
