NAME = 'it/pipelineManager'
DSL = '''pipeline {
  agent any
  stages {
    stage('pipelineManager') {
      steps {
        sleep randomNumber(min: 5, max: 30)
        pipelineManager(cancelPreviousRunningBuilds: [ when: 'ALWAYS' ])
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
