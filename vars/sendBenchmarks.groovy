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
  Send the benchmarks to the cloud service or run the script and prepare the environment
  to be implemented within the script itself.

  sendBenchmarks()
  sendBenchmarks(file: 'bench.out', index: 'index-name')

  sendBenchmarks.prepareAndRun(secret: 'foo', url_var: 'ES_URL', user_var: "ES_USER", pass_var: 'ES_PASS')
*/
def call(Map params = [:]) {
  if(!isUnix()){
    error('sendBenchmarks: windows is not supported yet.')
  }
  def benchFile = params.containsKey('file') ? params.file : 'bench.out'
  def index = params.containsKey('index') ? params.index : 'benchmark-go'
  def secret = params.containsKey('secret') ? params.secret : 'secret/apm-team/ci/java-agent-benchmark-cloud'
  def archive = params.containsKey('archive') ? params.archive : true

  if(archive){
    archiveArtifacts(allowEmptyArchive: true,
      artifacts: benchFile,
      onlyIfSuccessful: false)
  }

  def props = getVaultSecret(secret: secret)
  if(props?.errors){
     error "Benchmarks: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def data = props?.data
  def user = data?.user
  def password = data?.password

  if(data == null || user == null || password == null){
    error "Benchmarks: was not possible to get authentication info to send benchmarks"
  }

  def url = params.containsKey('url') ? params.url : data.url
  def protocol = getProtocol(url)
  url = url - protocol
  def urlAuth = "${protocol}${user}:${password}@${url}"

  log(level: 'INFO', text: "Benchmarks: sending data...")
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
        "CLOUD_PASSWORD=${password}",
        "BENCH_FILE=${benchFile}",
        "INDEX=${index}"]){
          if(index.equals('benchmark-go') || index.equals('benchmark-server')){
            sh label: 'Sending benchmarks', script: '''#!/bin/bash
            set +x -euo pipefail
            GO_VERSION=${GO_VERSION:-"1.10.3"}
            export GOPATH=${WORKSPACE}
            export PATH=${GOPATH}/bin:${PATH}
            eval "$(gvm ${GO_VERSION})"

            go get -v -u github.com/elastic/gobench
            gobench -index ${INDEX} -es "${CLOUD_URL}" < ${BENCH_FILE}
            '''
          } else {
            def datafile = readFile(file: "${BENCH_FILE}")
            def messageBase64UrlPad = base64encode(text: "${CLOUD_USERNAME}:${CLOUD_PASSWORD}", encoding: "UTF-8")

            httpRequest(url: "${CLOUD_ADDR}/_bulk", method: "POST",
                headers: [
                    "Content-Type": "application/json",
                    "Authorization": "Basic ${messageBase64UrlPad}"],
                data: datafile.toString() + "\n")
          }
      }
   }
}

/**
 This will allow to encapsulate the credentials and use them within the script which
 runs in the body
*/
def prepareAndRun(Map params = [:], Closure body) {
  if(!isUnix()){
    error('prepareAndRun: windows is not supported yet.')
  }
  def secret = params.containsKey('secret') ? params.secret : error('prepareAndRun: secret argument is required.')
  def urlVar = params.containsKey('url_var') ? params.url_var : error('prepareAndRun: url_var argument is required.')
  def userVar = params.containsKey('user_var') ? params.user_var : error('prepareAndRun: user_var argument is required.')
  def passVar = params.containsKey('pass_var') ? params.pass_var : error('prepareAndRun: pass_var argument is required.')

  def props = getVaultSecret(secret: secret)
  if(props?.errors){
     error "prepareAndRun: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def data = props?.data
  def url = data?.url
  def user = data?.user
  def password = data?.password

  if(data == null || user == null || password == null || url == null){
    error "prepareAndRun: was not possible to get authentication info to send benchmarks"
  }

  def protocol = getProtocol(url)

  log(level: 'INFO', text: 'sendBenchmark: run script...')
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: urlVar, password: "${protocol}${url}"],
    [var: userVar, password: "${user}"],
    [var: passVar, password: "${password}"]
    ]]) {
    withEnv(["${urlVar}=${protocol}${url}", "${userVar}=${user}", "${passVar}=${password}"]){
      body()
    }
  }
}

def getProtocol(url){
  def protocol = "https://"
  if(url.startsWith("https://")){
    protocol = "https://"
  } else if (url.startsWith("http://")){
    log(level: 'INFO', text: "Benchmarks: you are using 'http' protocol to access to the service.")
    protocol = "http://"
  } else {
    error "Benchmarks: unknow protocol, the url is not http(s)."
  }
  return protocol
}
