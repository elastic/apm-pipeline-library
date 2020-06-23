NAME = 'it/superLinter'
DSL = '''
pipeline {
  agent any
  stages {
    stage('super-linter') {
      steps {
        deleteDir()
        gitCheckout(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    repo: 'https://github.com/elastic/apm-pipeline-library.git',
                    branch: master,
                    basedir: 'sub-folder')
        superLinter(envs: [ 'VALIDATE_GO=false' ], failNever: true)
      }
    }
  }
}
'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
