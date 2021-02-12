NAME = 'it/githubCheck'
DSL = '''pipeline {
  agent { label "linux" }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }   
  stages {
    stage('githubCheck') {
      steps {
        githubCheck(name: 'githubCheck-step',
                    description: 'Execute something',
                    org: 'elastic',
                    repository: 'apm-pipeline-library',
                    commitId: '69e3ea411a363ec113766c78a29d4091c6dd2760',
                    status: 'failure',
                    detailsUrl: "${env.BUILD_URL}",
                    body: """### MARKDOWN \n\n #### Snippet \n ```bash\ntest```"""
        )
        withGithubCheck(context: 'withGithubCheck-step',
                        description: 'Execute something',
                        org: 'elastic',
                        repository: 'apm-pipeline-library',
                        commitId: '69e3ea411a363ec113766c78a29d4091c6dd2760',
                        status: 'failure',
                        detailsUrl: "${env.BUILD_URL}",
                        body: """### MARKDOWN \n\n #### Snippet \n ```bash\ntest```""") {
                            echo 'ERROR 1'
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
