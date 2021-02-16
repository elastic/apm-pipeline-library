NAME = 'it/googleStorageUploadExt'
DSL = '''pipeline {
  agent { label "master" }
  environment {
    JOB_GCS_BUCKET = 'apm-ci-temp'
    JOB_GCS_CREDENTIALS = 'apm-ci-gcs-plugin-file-credentials'
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }
  stages {
    stage('google-storage') {
      steps {
        touch file: 'file.txt', timestamp: 0
        googleStorageUploadExt(bucket: "gs://${env.JOB_GCS_BUCKET}/test-${env.BUILD_ID}/", pattern: 'file.txt', sharedPublicly: true)
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
