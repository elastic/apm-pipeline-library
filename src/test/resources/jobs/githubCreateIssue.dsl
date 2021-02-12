NAME = 'it/githubCreateIssue'
DSL = '''pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    repo: 'https://github.com/elastic/apm-pipeline-library.git',
                    branch: 'master',
                    basedir: 'sub-folder')
        dir('sub-folder') {
          githubCreateIssue(title: 'Foo', description: 'Bar', labels: 'invalid', milestone: 'chore')
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
