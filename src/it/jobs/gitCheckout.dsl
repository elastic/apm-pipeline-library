NAME = 'it/gitCheckout'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(basedir: 'sub-folder', branch: 'master', repo: 'https://github.com/octocat/Hello-World.git',
                    credentialsId: 'v1v-pat')  // TODO: required to create the PAT credentials with the JCasC
        sh 'ls -ltra'
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
