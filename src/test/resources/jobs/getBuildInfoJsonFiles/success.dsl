NAME = 'it/getBuildInfoJsonFiles/success'
DSL = '''pipeline {
  agent any
  stages {
    stage('success') {
      steps { echo 'hi' }
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
