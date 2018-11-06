/**
  Send the becnhmarks to the cloud service.
  requires Go installed.
  
  sendBenchmarks()
  sendBenchmarks(file: 'bench.out')
*/
def call(Map params = [:]) {
  def benchFile = params.containsKey('file') ? params.file : 'bench.out'
  def index = params.containsKey('index') ? params.index : 'benchmark-go'

  //apm-server-benchmark-cloud
  //java-agent-benchmark-cloud
  //https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243/_bulk
  //https://5492443829134f71a94c96689e9db66e.europe-west3.gcp.cloud.es.io:9243
  //curl --user ${CLOUD_USERNAME}:${CLOUD_PASSWORD} -XPOST 'https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243/_bulk' -H 'Content-Type: application/json'  --data-binary @${BULK_UPLOAD_FILE}
  def props = getVaultSecret('java-agent-benchmark-cloud')
  if(props?.errors){
     error "Benchmarks: Unable to get credentials from the vault: " + props.errors.toString()
  } else {
    def data = props?.data
    def user = data?.user
    def password = data?.password
    def url = "1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243"
    def urlAuth = "https://${user}:${password}@${url}"
    
    if(data == null || user == null || password == null){
      error "Benchmarks: was not possible to get authentication info to send benchmarks"
    }
    echo "Benchmarks: sending data..."
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
      [var: 'CLOUD_URL', password: "${urlAuth}"],
      [var: 'CLOUD_ADDR', password: "${url}"],
      [var: 'CLOUD_USERNAME', password: "${user}"],
      [var: 'CLOUD_PASSWORD', password: "${password}"],
      ]]) {
         sh """#!/bin/bash
         set +x -euo pipefail
         
         export GOPATH=\${WORKSPACE}
         export PATH=\${GOPATH}/bin:\${PATH}
         eval "\$(gvm \${GO_VERSION})"
         
         go get -v -u github.com/elastic/gobench
         gobench -index ${index} -es "${urlAuth}" < ${benchFile}
         """
     }
  }
}