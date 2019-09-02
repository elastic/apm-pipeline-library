NAME = 'it/isTimerTrigger'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(basedir: 'sub-folder', branch: 'master', repo: 'https://github.com/octocat/Hello-World.git',
                    credentialsId: 'v1v-pat')  // TODO: required to create the PAT credentials with the JCasC
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
