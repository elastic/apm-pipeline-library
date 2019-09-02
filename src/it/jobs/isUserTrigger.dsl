NAME = 'it/isUserTrigger'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(basedir: 'sub-folder', branch: 'master', credentialsId: 'pat',
                    repo: 'https://github.com/octocat/Hello-World.git')
      }
    }
    stage('isUserTrigger') {
      steps {
        script {
          if (isUserTrigger()) {
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
