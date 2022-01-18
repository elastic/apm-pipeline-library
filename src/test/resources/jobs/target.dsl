NAME = 'target'
DSL = '''pipeline {
  agent none
  stages {
    stage('hello') {
      steps {
        echo 'hello'
      }
    }
  }
}'''

pipelineJob(NAME) {
  authenticationToken('secret')
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
