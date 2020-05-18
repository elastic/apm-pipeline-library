NAME = 'it/getBeatsModule'
DSL = '''pipeline {
  agent any
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }
  stages {
    stage('checkout') {
      steps {
        gitCheckout(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    repo: 'https://github.com/elastic/apm-pipeline-library.git',
                    branch: 'master',
                    basedir: 'sub-folder',
                    shallow: false)
      }
    }
    stage('match') {
      steps {
        dir('sub-folder') {
          script {
            def module = getBeatsModule(pattern: '^\\\\.ci\\\\/([^\\\\/]+)\\\\/.*',
                                        from: '347185bd7e2b402ba8f6befa5ef4428ad417fbbc',
                                        to: '4d9fc25d258622c767ec4d38df38520647cc7dda')
            whenFalse(module.equals('jobs')){
              error("Expected module name 'jobs'")
            }
          }
        }
      }
    }
    stage('no_match') {
      steps {
        dir('sub-folder') {
          script {
            def module = getBeatsModule(pattern: '^\\\\ci\\\\/([^\\\\/]+)\\\\/.*',
                                        from: '347185bd7e2b402ba8f6befa5ef4428ad417fbbc',
                                        to: '4d9fc25d258622c767ec4d38df38520647cc7dda')
            whenFalse(module.equals('')){
              error("Expected module name ''")
            }
          }
        }
      }
    }
    stage('match_with_exclude') {
      steps {
        dir('sub-folder') {
          script {
            def module = getBeatsModule(pattern: '^test-infra\\\\/([^\\\\/]+)\\\\/.*',
                                        from: 'b0de59d0ec1e2ae52103a238a2e9cb5b0d7fd9b8',
                                        to: '641fd600836abafa51def05260d63fab6eed4707',
                                        exclude: '^resources.*')
            whenFalse(module.equals('apm-ci')){
              error("Expected module name 'apm-ci'")
            }
          }
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
