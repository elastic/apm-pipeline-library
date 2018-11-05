#!/usr/bin/env groovy

/**
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
  /* TODO replace each variable with a secret text credential type, then use withCredentials step.
  https://jenkins.io/doc/book/pipeline/jenkinsfile/#handling-credentials
  withCredentials([string(credentialsId: '6a80d11c-cb5f-4e40-8565-78e127610ef1', variable: 'VAULT_ROLE_ID_HEY_APM')]) {
    // some block
  }
  
  ansible/roles/jenkins/templates/credentials.xml.j2
  
  {% if 'apm-ci' in group_names %}
<!-- Credentials to use in Pipelines to avoid leak sensible data -->
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl plugin="plain-credentials@1.4">
  <scope>GLOBAL</scope>
  <id>VAULT_ROLE_ID_HEY_APM</id>
  <description>Vault Role ID to use witht the Hey APM test</description>
  <secret></secret>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl plugin="plain-credentials@1.4">
  <scope>GLOBAL</scope>
  <id>VAULT_SECRET_ID_HEY_APM</id>
  <description>Vault Secret ID to use witht the Hey APM test</description>
  <secret></secret>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl plugin="plain-credentials@1.4">
  <scope>GLOBAL</scope>
  <id>VAULT_SECRET_ID</id>
  <description>Vault Secret Id for general use</description>
  <secret></secret>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl plugin="plain-credentials@1.4">
  <scope>GLOBAL</scope>
  <id>VAULT_ADDR</id>
  <description>Vault URL</description>
  <secret></secret>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl plugin="plain-credentials@1.4">
  <scope>GLOBAL</scope>
  <id>JOB_GCS_BUCKET</id>
  <description>Google Cloud Service Bucket to store artifacts</description>
  <secret></secret>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
{% endif %}
  */
}