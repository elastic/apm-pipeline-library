NAME = 'it/git'
DSL = '''pipeline {
  agent any
  stages {
    stage('git') {
      steps {
        git 'https://github.com/jglick/simple-maven-project-with-tests.git'
        deleteDir()
      }
    }
    stage('git_with_credentials') {
      steps {
        git credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
            url: 'https://github.com/jglick/simple-maven-project-with-tests.git',
            branch: 'master'
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
