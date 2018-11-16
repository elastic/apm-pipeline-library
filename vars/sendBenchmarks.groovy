/**
  Send the becnhmarks to the cloud service.
  requires Go installed.
  
  sendBenchmarks()
  sendBenchmarks(file: 'bench.out', index: 'index-name')
*/
def call(Map params = [:]) {
  def benchFile = params.containsKey('file') ? params.file : 'bench.out'
  def index = params.containsKey('index') ? params.index : 'benchmark-go'
  def url = params.containsKey('url') ? params.url : "https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243"
  def secret = params.containsKey('secret') ? params.secret : 'java-agent-benchmark-cloud'
  def bulk = params.containsKey('bulk') ? params.bulk : false

  //apm-server-benchmark-cloud
  //java-agent-benchmark-cloud
  //https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243/_bulk
  //https://5492443829134f71a94c96689e9db66e.europe-west3.gcp.cloud.es.io:9243
  //curl --user ${CLOUD_USERNAME}:${CLOUD_PASSWORD} -XPOST 'https://1ec92c339f616ca43771bff669cc419c.europe-west3.gcp.cloud.es.io:9243/_bulk' -H 'Content-Type: application/json'  --data-binary @${BULK_UPLOAD_FILE}
  def props = getVaultSecret(secret)
  if(props?.errors){
     error "Benchmarks: Unable to get credentials from the vault: " + props.errors.toString()
  } 
  
  if(bulk){
    url += '/_bulk'
  }
  
  def protocol = "https://"
  if(url.startsWith("https://")){
    url = url - "https://"
    protocol = "https://"
  } else if (url.startsWith("http://")){
    echo "withEsEnv: you are using 'http' protocol to access to the service."
    url = url - "http://"
    protocol = "http://"
  } else {
    error "Benchmarks: unknow protocol, the url is not http(s)."
  }
  
  def data = props?.data
  def user = data?.user
  def password = data?.password
  def urlAuth = "${protocol}${user}:${password}@${url}"
  
  if(data == null || user == null || password == null){
    error "Benchmarks: was not possible to get authentication info to send benchmarks"
  }
  echo "Benchmarks: sending data..."
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'CLOUD_URL', password: "${urlAuth}"],
    [var: 'CLOUD_ADDR', password: "${protocol}${url}"],
    [var: 'CLOUD_USERNAME', password: "${user}"],
    [var: 'CLOUD_PASSWORD', password: "${password}"],
    ]]) {
       sh """#!/bin/bash
       set +x -euo pipefail
       GO_VERSION=\${GO_VERSION:-"1.10.3"}
       export GOPATH=\${WORKSPACE}
       export PATH=\${GOPATH}/bin:\${PATH}
       eval "\$(gvm \${GO_VERSION})"
       
       go get -v -u github.com/elastic/gobench
       gobench -index ${index} -es "${urlAuth}" < ${benchFile}
       """
   }
}