NAME = 'it/withGitRelease'
DSL = '''
pipeline {
  agent any
  environment {
    // Simple pipelines don't have the BRANCH_NAME variable
    BRANCH_NAME = 'master'
  }
  stages {
    stage('checkout') {
      steps {
        deleteDir()
        gitCheckout(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    repo: 'https://github.com/elastic/apm-pipeline-library.git',
                    branch: BRANCH_NAME,
                    basedir: 'sub-folder')
      }
    }
    stage('withGitRelease') {
      steps {
        dir('sub-folder') {
          // Ensure the git config is configured as expected beforehand
          sh "git config -l | grep '^remote.*.url' | grep 'https://github.com/elastic/apm-pipeline-library.git'"
          withGitRelease() {
            sh "git config -l | grep '^remote.*.url'"
          }
          // Ensure the git config is reverted as used to be
          sh "git config -l | grep '^remote.*.url' | grep 'https://github.com/elastic/apm-pipeline-library.git'"
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
