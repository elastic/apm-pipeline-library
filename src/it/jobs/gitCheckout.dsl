NAME = 'it/gitCheckout'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(basedir: 'sub-folder', branch: 'master',
                    repo: 'git@github.com:octocat/Hello-World.git',
                    credentialsId: 'v1v-pat',  // TODO: required to create the PAT credentials with the JCasC
                    reference: '/var/lib/jenkins/Hello-World.git')
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

queue(NAME)
