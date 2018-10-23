/**
  Get the credentials to access to the ES benchmarks service, and execute some block of code with
  the environment variables CLOUD_USERNAME, CLOUD_PASSWORD, CLOUD_ADDR, and CLOUD_URL defined.
 }.
 
withEnvBenchmarksData {
  //body
}
*/
def call(Closure body) {
  def props = null
  withEnv([
   'VAULT_ROLE_ID_BENCH=35ad5918-eab7-c814-f8be-a305c811732e', 
   'VAULT_SECRET_ID_BENCH=95d18733-44b5-53c3-89c5-91e27b29be4f', 
   'VAULT_ADDR=https://secrets.elastic.co:8200']){
     def cloudData = sh(returnStdout:true, script: '''#!/bin/bash
     set +x
     VAULT_TOKEN=$( curl -s -X POST -H "Content-Type: application/json" -L -d "{\"role_id\":\"${VAULT_ROLE_ID_BENCH}\",\"secret_id\":\"${VAULT_SECRET_ID_BENCH}\"}" ${VAULT_ADDR}/v1/auth/approle/login | jq -r '.auth.client_token' )
     curl -s -L -H "X-Vault-Token:${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/apm-team/ci/java-agent-benchmark-cloud
     ''')
     props = readJSON(text: cloudData)
   }
  if(props?.errors){
     error "Unable to get credentials from the vault: " + props.errors.toString()
  } else {
     withEnv([
       "CLOUD_USERNAME=${props?.data?.user}",
       "CLOUD_PASSWORD=${props?.data?.password}",
       "CLOUD_ADDR=${props?.data.url}",
       "CLOUD_URL=https://${props?.data?.user}:${props?.data?.password}@props?.data?.url"]){
       sh 'export'
     }
  }
}