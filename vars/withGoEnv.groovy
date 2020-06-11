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
 Install Go an run some command in a pre-configured environment.

  withGoEnv(version: '1.14.2'){
    sh(label: 'Go version', script: 'go version')
  }

   withGoEnv(version: '1.14.2', pkgs: [
       "github.com/magefile/mage",
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       sh(label: 'Run mage',script: 'mage -version')
   }

}

*/
def call(Map args = [:], Closure body) {
  def goDefaultVersion = "" != "${env.GO_VERSION}" && env.GO_VERSION != null ? "${env.GO_VERSION}" : '1.14.2'
  def version = args.containsKey('version') ? args.version : goDefaultVersion
  def pkgs = args.containsKey('pkgs') ? args.pkgs : []
  def os = nodeOS()
  def lastCoordinate = version[-2..-1]
  // gvm remove the last coordinate if it is 0
  def goDir = ".gvm/versions/go${lastCoordinate != ".0" ? version : version[0..-3]}.${os}.amd64"
  withEnv([
      "HOME=${env.WORKSPACE}"
  ]){
    withEnv([
      "PATH=${HOME}/bin:${HOME}/${goDir}/bin:${env.PATH}",
      "GOROOT=${HOME}/${goDir}",
      "GOPATH=${HOME}"
    ]){
      retryWithSleep(retries: 3, seconds: 5, backoff: true){
        sh(label: "Installing go ${version}", script: "gvm ${version}")
      }
      pkgs?.each{ p ->
        retryWithSleep(retries: 3, seconds: 5, backoff: true){
          sh(label: "Installing ${p}", script: "go get -u ${p}")
        }
      }
      body()
    }
  }
}
