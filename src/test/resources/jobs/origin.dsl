NAME = 'origin'
DSL = '''pipeline {
  agent any
  stages {
    stage('hello') {
      steps {
        triggerRemoteJob(httpGetReadTimeout: 1000,
                         job: 'target',
                         pollInterval: 5,
                         remoteJenkinsName: 'localhost',
                         token: 'secret',
                         useCrumbCache: true,
                         useJobInfoCache: true)
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
