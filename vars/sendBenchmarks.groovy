/**
 Send the becnhmarks to the cloud service.
 
 sendBenchmarks()
 sendBenchmarks(file: 'bench.out')
*/
def call(Map params = [:]) {
  def benchFile = params.containsKey('file') ? params.file : 'bench.out'
  
  //apm-server-benchmark-cloud
  //java-agent-benchmark-cloud
  //https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243/_bulk
  //https://5492443829134f71a94c96689e9db66e.europe-west3.gcp.cloud.es.io:9243
  //curl --user ${CLOUD_USERNAME}:${CLOUD_PASSWORD} -XPOST 'https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243/_bulk' -H 'Content-Type: application/json'  --data-binary @${BULK_UPLOAD_FILE}
  def props = getVaultSecret('java-agent-benchmark-cloud')
  if(props?.errors){
     error "Unable to get credentials from the vault: " + props.errors.toString()
  } else {
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
      [var: 'CLOUD_USERNAME', password: "${props?.data?.user}"], 
      [var: 'CLOUD_PASSWORD', password: "${props?.data?.password}"], 
      [var: 'CLOUD_ADDR', password: "${props?.data.url}"], 
      [var: 'CLOUD_URL', password: "https://${props?.data?.user}:${props?.data?.password}@props?.data?.url"], 
      ]]) {
         sh """#!/bin/bash
         set +x
         go get -v -u github.com/elastic/gobench
         \${GOPATH}/bin/gobench -index benchmark-go -es "\${CLOUD_URL}" < ${benchFile}
         """
     }
  }
}