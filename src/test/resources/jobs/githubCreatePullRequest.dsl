NAME = 'it/githubCreatePullRequest'
DSL = '''
pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        gitCheckout(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    repo: 'https://github.com/elastic/apm-pipeline-library.git',
                    branch: 'master',
                    basedir: 'sub-folder')
        dir('sub-folder') {
          setupAPMGitEmail(global: true)
          sh(script: git checkout -b githubCreatePullRequest-$(date "+%Y%m%d%H%M%S") && touch githubCreatePullRequest.txt && git add githubCreatePullRequest.txt && git commit -m 'chore: for testing purposes')
          githubCreatePullRequest(title: 'Foo', description: 'Bar', labels: 'invalid', milestone: 'chore', assigne: 'v1v', draft: true)
        }
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
