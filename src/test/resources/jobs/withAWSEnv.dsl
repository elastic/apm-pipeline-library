NAME = 'it/withAWSEnv'
DSL = '''pipeline {
  agent any
  stages {
    stage('withAWSEnv') {
      steps {
        withAWSEnv(secret: 'secret/observability-team/ci/service-account/aws-create-user') {{
          sh 'aws --version'
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
