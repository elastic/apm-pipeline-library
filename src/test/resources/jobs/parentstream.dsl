NAME = 'it/parentstream'
DSL = '''pipeline {
  agent any
  stages {
    stage('trigger downstream') {
      steps {
        script {
          try {
            build job: 'downstream'
          } catch(e) {
            println e.getCauses()[0]?.getShortDescription()
          }
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
