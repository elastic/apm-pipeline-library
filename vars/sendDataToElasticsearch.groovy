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
  Send the JSON report file to Elastisearch.

  sendDataToElasticsearch(es: "https://ecs.example.com:9200", secret: "secret", data: '{"field": "value"}')
*/
def call(Map params = [:]){
  def es = params.containsKey('es') ? params.es : error("sendDataToElasticsearch: Elasticsearch URL is not valid.")
  def secret = params.containsKey('secret') ? params.secret : error("sendDataToElasticsearch: secret is not valid.")
  def data = params.containsKey('data') ? params.data : error("sendDataToElasticsearch: data is not valid.")
  def restCall = params.containsKey('restCall') ? params.restCall : "/jenkins-builds/_doc/"
  def contentType = params.containsKey('contentType') ? params.contentType : "application/json"
  def method = params.containsKey('method') ? params.method : "POST"

  def props = getVaultSecret(secret: secret)
  if(props?.errors){
     error "sendDataToElasticsearch: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def value = props?.data
  def user = value?.user
  def password = value?.password
  if(data == null || user == null || password == null){
    error "sendDataToElasticsearch: was not possible to get authentication info to send data."
  }

  log(level: 'INFO', text: "sendDataToElasticsearch: sending data...")

  def messageBase64UrlPad = base64encode(text: "${user}:${password}", encoding: "UTF-8")
  return httpRequest(url: "${es}${restCall}", method: "${method}",
      headers: [
          "Content-Type": "${contentType}",
          "Authorization": "Basic ${messageBase64UrlPad}"],
      data: data.toString() + "\n")
}
