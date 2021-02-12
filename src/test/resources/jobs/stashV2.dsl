NAME = 'it/stashV2'
DSL = '''pipeline {
  agent any
  stages {
    stage('stashV2') {
      steps {
        // Credentials for the Google Storage are file based, so it's not
        // fully automated yet, for such you need to create a new credentials
        // manually
        input 'Please create Google Service account from private key  http://localhost:18080/credentials/store/system/domain/_/newCredentials'
        gitCheckout(basedir: 'sub-folder', branch: 'master',
                    credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    repo: 'https://github.com/octocat/Hello-World.git')
        withEnv(["JOB_GCS_BUCKET=beats-ci-temp", "JOB_GCS_CREDENTIALS=beats-ci-gcs-plugin"]){
          stashV2(name: 'source')
        }
        stashV2(name: 'foo', bucket: 'beats-ci-temp', credentialsId: 'beats-ci-gcs-plugin')
        deleteDir()
      }
    }
    stage('unstashV2'){
      steps {
        dir('source') {
          withEnv(['JOB_GCS_BUCKET=beats-ci-temp', 'JOB_GCS_CREDENTIALS=beats-ci-gcs-plugin']){
            unstashV2(name: 'source')
          }
          sh 'ls -ltra sub-folder'
        }
        dir('foo') {
          unstashV2(name: 'foo', bucket: 'beats-ci-temp', credentialsId: 'beats-ci-gcs-plugin')
          sh 'ls -ltra sub-folder'
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
