NAME = 'it/runWatcher'
DSL = '''pipeline {
  agent any
  stages {
    stage('runWatcher') {
      steps {
        runWatcher(watcher: '17635395-61cd-439a-963d-8e7bb6ab22b7')
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
