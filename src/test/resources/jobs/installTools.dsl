NAME = 'it/installTools'
DSL = '''pipeline {
  agent none
  stages {
    stage('windows') {
      agent { label 'windows-immutable' }
      steps {
        deleteDir()
        installTools([ [ tool: 'python3', version: '3.8'] ])
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
