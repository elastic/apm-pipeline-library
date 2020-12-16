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
 Install Go and run some command in a pre-configured environment for Windows.

  withGoEnvWindows(version: '1.14.2'){
    bat(label: 'Go version', script: 'go version')
  }

   withGoEnvWindows(version: '1.14.2', pkgs: [
       "github.com/magefile/mage",
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       bat(label: 'Run mage',script: 'mage -version')
   }

}

*/
def call(Map args = [:], Closure body) {
  def goDefaultVersion = "" != "${env.GO_VERSION}" && env.GO_VERSION != null ? "${env.GO_VERSION}" : '1.14.2'
  def version = args.containsKey('version') ? args.version : goDefaultVersion
  def pkgs = args.containsKey('pkgs') ? args.pkgs : []
  def os = args.containsKey('os') ? args.os : nodeOS()
  def mingwArch = is32() ? '32' : '64'
  def goArch = is32() ? '386' : 'amd64'
  def chocoPath = 'C:\\ProgramData\\chocolatey\\bin'
  def userProfile="${env.WORKSPACE}"
  // gvm remove the last coordinate if it is 0
  def lastCoordinate = version[-2..-1]
  def goDir = ".gvm\\versions\\go${lastCoordinate != ".0" ? version : version[0..-3]}.${os}.${goArch}"
  def goRoot = "${userProfile}\\${goDir}"
  def path = "${env.WORKSPACE}\\bin;${goRoot}\\bin;${chocoPath};C:\\tools\\mingw${mingwArch}\\bin;${env.PATH}"

  withEnv([
    "HOME=${env.WORKSPACE}",
    "PATH=${path}",
    "GOROOT=${goRoot}",
    "GOPATH=${env.WORKSPACE}",
    "USERPROFILE=${userProfile}"
  ]){
    def content = libraryResource('scripts/install-tools.bat')
    retryWithSleep(retries: 2, seconds: 5, backoff: true){
      bat(label: "Installing Go ${version}", script: content)
    }
    pkgs?.each{ p ->
      retryWithSleep(retries: 3, seconds: 5, backoff: true){
        bat(label: "Installing ${p}", script: "go get -u ${p}")
      }
    }
    body()
  }
}
