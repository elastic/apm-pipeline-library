NAME = 'it/writeVaultSecret'
DSL = '''pipeline {
  agent any
  stages {
    stage('isAllowed') {
      steps {
        writeVaultSecret(secret: 'secret/observability-team/ci/temp/github-comment',
                         data: ['secret': "${BUILD_ID}"] )
        script {
          data = getVaultSecret(secret: 'secret/observability-team/ci/temp/github-comment')
          if (data.data.secret.contains("${BUILD_ID}")) {
            echo 'Assertion passed'
          } else {
            error('Assertion failed')
          }
        }
      }
    }
    stage('NotAllowed') {
      steps {
        script {
          try {
            writeVaultSecret(secret: 'secret/observability-team/ci/github-comment',
                            data: ['secret': 'bar'] )
            error('Assertion failed')
          } catch(e) {
            echo 'Assertion passed'
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
