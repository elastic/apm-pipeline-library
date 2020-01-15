NAME = 'it/parentstream'
DSL = '''pipeline {
  agent any
  stages {
    stage('trigger downstream') {
      steps {
        build job: 'downstream'
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
