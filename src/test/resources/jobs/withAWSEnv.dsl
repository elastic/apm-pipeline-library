NAME = 'it/withAWSEnv'
DSL = '''pipeline {
  agent any
  stages {
    stage('withAWSEnv') {
      steps {
        withAWSEnv(secret: 'secret/observability-team/ci/service-account/s3-artifact-manager-observability-ci') {{
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
