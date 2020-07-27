NAME = 'it/beats/beatsWhen'
DSL = '''pipeline {
  agent none
  stages {
    stage('linux') {
      agent { label 'linux && immutable' }
      steps {
        beatsWhen(project: 'test',
                  content: 'TBD')
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
