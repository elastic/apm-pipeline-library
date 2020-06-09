NAME = 'checkGithubQuota'
DSL = '''pipeline {
   agent { label 'linux && immutable' }
   stages {
      stage('check') {
         steps {
            withCredentials([
              usernamePassword(
                credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                passwordVariable: 'PASSWORD',
                usernameVariable: 'USERNAME'
              )
            ]) {
              sh 'set +x;curl -sSfL -u ${USERNAME}:${PASSWORD} https://api.github.com/rate_limit'
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
