#!/usr/bin/env groovy

/**
  Environment wrapper that mask some environment variables and install some tools.
  
  withEnvWrapper(){
    //block
  }
*/
def call(Closure body) {
  wrap([$class: 'MaskPasswordsBuildWrapper', 
    varPasswordPairs: [
      [var: 'JOB_GCS_CREDENTIALS', password: 'apm-ci-gcs-plugin'], 
      [var: 'JOB_GCS_BUCKET', password: 'apm-ci-artifacts/jobs'], 
      [var: 'NOTIFY_TO', password: 'infra-root+build@elastic.co']
    ],
    varMaskRegexes: [[regex: 'http(s)?\\:\\/+(.*)\\.elastic\\.co']]
    ]) {
    deleteDir()
    withEnv([
      "JOB_GCS_CREDENTIALS=apm-ci-gcs-plugin",
      "JOB_GCS_BUCKET=apm-ci-artifacts/jobs",
      "NOTIFY_TO=infra-root+build@elastic.co"
      ]){
        body()
      }
  }
}