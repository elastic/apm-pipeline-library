NAME = 'it/isTimerTrigger'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(basedir: 'sub-folder', branch: 'master', credentialsId: 'pat',
                    repo: 'https://github.com/octocat/Hello-World.git')
      }
    }
    stage('isTimerTrigger') {
      steps {
        script {
          if (isTimerTrigger()) {
            echo 'found'
          } else {
            echo 'not found'
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
