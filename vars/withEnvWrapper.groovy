#!/usr/bin/env groovy

/**
withEnvWrapper(){
  //block
}
*/
def call(Closure body) {
  ansiColor('xterm') {
    withEnv([
      'VAULT_ROLE_ID_HEY_APM=35ad5918-eab7-c814-f8be-a305c811732e', 
      'VAULT_SECRET_ID_HEY_APM=95d18733-44b5-53c3-89c5-91e27b29be4f', 
      'VAULT_SECRET_ID=c50f028c-ba5d-a921-7869-c63966e3cd79', 
      'VAULT_ADDR=https://secrets.elastic.co:8200', 
      'JOB_GCS_CREDENTIALS=jenkins-gcs-plugin', 
      'JOB_GCS_BUCKET=apm-ci-artifacts/jobs', 
      'JOB_GIT_CREDENTIALS=f6c7695a-671e-4f4f-a331-acdce44ff9ba',
      'NOTIFY_TO=infra-root+build@elastic.co']) {
        deleteDir()
        try {
          body()
        } catch (err) {
          echo "${err}"
          throw err
        }
    }
  }
  /* TODO replace each variable with a secret text credential type, then use withCredentials step.
  https://jenkins.io/doc/book/pipeline/jenkinsfile/#handling-credentials
  withCredentials([string(credentialsId: '6a80d11c-cb5f-4e40-8565-78e127610ef1', variable: 'VAULT_ROLE_ID_HEY_APM')]) {
    // some block
  }
  */
}