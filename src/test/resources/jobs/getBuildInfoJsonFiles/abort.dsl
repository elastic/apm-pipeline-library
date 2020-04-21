NAME = 'it/getBuildInfoJsonFiles/abort'
DSL = '''pipeline {
  agent any
  options {
    timeout(time: 10, unit: 'SECONDS')
  }
  stages {
    stage('timeout') {
      steps { sleep 30 }
    }
  }
  post {
    cleanup {
      deleteDir()
      getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
      archiveArtifacts artifacts: '*.json'
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
