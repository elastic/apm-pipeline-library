NAME = 'it/githubCheck'
DSL = '''pipeline {
  agent { label "linux" }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }   
  stages {
    stage('githubCheck') {
      steps {
        githubCheck(name: 'name',
                    description: 'Execute something',
                    org: "v1v",
                    repository: "ansible-role-jenkins_plugin_cli",
                    commitId: "01bdb5ccf3a4a2028dab121bf892126b1db571d2",
                    status: 'failure',
                    body: "### MARKDOWN \n\n fooo ```bash\ntest```"
        )
        withGithubCheck(context: 'name',
                        description: 'Execute something',
                        org: "v1v",
                        repository: "ansible-role-jenkins_plugin_cli",
                        commitId: "01bdb5ccf3a4a2028dab121bf892126b1db571d2",
                        status: 'failure',
                        body: "### MARKDOWN \n\n fooo ```bash\ntest```") {
                            echo 'ERROR 1'
                        }
        )
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
