#!/usr/bin/env groovy

/**
  Grab a secret from the vault and define some environment variables to access to an URL

  withEsEnv(){
    //block
  }

  withEsEnv(url: 'https://url.exanple.com', secret: 'secret-name'){
    //block
  }
*/
def call(Map params = [:], Closure body) {
  def url = params.containsKey('url') ? params.url : 'https://5492443829134f71a94c96689e9db66e.europe-west3.gcp.cloud.es.io:9243'
  def secret = params?.secret
  
  def props = getVaultSecret(secret)
  if(props?.errors){
     error "withEsEnv: Unable to get credentials from the vault: " + props.errors.toString()
  }
  
  def protocol = "https://"
  if(url.startsWith("https://")){
    url = url - "https://"
    protocol = "https://"
  } else if (url.startsWith("http://")){
    log(level: 'INFO', text: "withEsEnv: you are using 'http' protocol to access to the service.")
    url = url - "http://"
    protocol = "http://"
  } else {
    error "withEsEnv: unknow protocol, the url is not http(s)."
  }
  
  def data = props?.data
  def user = data?.user
  def password = data?.password
  def urlAuth = "${protocol}${user}:${password}@${url}"
  
  if(data == null || user == null || password == null){
    error "withEsEnv: was not possible to get authentication info"
  }
  
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'CLOUD_URL', password: "${urlAuth}"],
    [var: 'CLOUD_ADDR', password: "${protocol}${url}"],
    [var: 'CLOUD_USERNAME', password: "${user}"],
    [var: 'CLOUD_PASSWORD', password: "${password}"],
    ]]) {
    withEnv([
      "CLOUD_URL=${urlAuth}",
      "CLOUD_ADDR=${protocol}${url}",
      "CLOUD_USERNAME=${user}",
      "CLOUD_PASSWORD=${password}"
      ]){
        body()
    }
   }
}