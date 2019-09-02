NAME = 'it/gitCheckout'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(basedir: 'sub-folder', branch: 'master', credentialsId: 'pat',
                    repo: 'https://github.com/octocat/Hello-World.git')
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
