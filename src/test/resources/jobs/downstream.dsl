NAME = 'it/downstream'
DSL = '''pipeline {
  agent any
  stages {
    stage('error') {
      steps {
        error 'force a build error'
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
