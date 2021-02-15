// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**
  Grab a secret from the vault and define some environment variables to access to an URL

  withEsEnv(){
    //block
  }

  withEsEnv(url: 'https://url.exanple.com', secret: 'secret-name'){
    //block
  }
*/
def call(Map args = [:], Closure body) {
  def url = args.containsKey('url') ? args.url : 'https://5492443829134f71a94c96689e9db66e.europe-west3.gcp.cloud.es.io:9243'
  def secret = args?.secret

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
