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
 Install Go and run some command in a pre-configured environment for Unix.

  withGoEnvUnix(version: '1.14.2'){
    sh(label: 'Go version', script: 'go version')
  }

   withGoEnvUnix(version: '1.14.2', pkgs: [
       "github.com/magefile/mage",
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       sh(label: 'Run mage',script: 'mage -version')
   }

}

*/
def call(Map args = [:], Closure body) {
  def version = args.containsKey('version') ? args.version : goDefaultVersion()
  def pkgs = args.containsKey('pkgs') ? args.pkgs : []
  def os = args.containsKey('os') ? args.os : nodeOS()
  def arch = (isArm()) ? 'arm64' : 'amd64'
  def lastCoordinate = version[-2..-1]
  // gvm remove the last coordinate if it is 0
  def goDir = ".gvm/versions/go${lastCoordinate != ".0" ? version : version[0..-3]}.${os}.${arch}"
  withEnv([
    "HOME=${env.WORKSPACE}",
    "PATH=${env.WORKSPACE}/bin:${env.WORKSPACE}/${goDir}/bin:${env.PATH}",
    "GOROOT=${env.WORKSPACE}/${goDir}",
    "GOPATH=${env.WORKSPACE}"
  ]){
    installGo(version: version)
    installPackages(pkgs: pkgs)
    debugGoEnv()
    body()
  }
}

def installGo(Map args = [:]) {
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    sh(label: "Installing go ${args.version}", script: "gvm ${args.version}")
  }
}

def installPackages(Map args = [:]) {
  // GOARCH is required to be able to install the given packages for the specific arch
  def arch = (env.GOARCH?.trim()) ?: goArch()
  log(level: 'DEBUG', text: "installPackages: GOARCH=${GOARCH}")
  withEnv(["GOARCH=${arch}"]){
    args.pkgs?.each{ p ->
      retryWithSleep(retries: 3, seconds: 5, backoff: true){
        sh(label: "Installing ${p}", script: "go get -u ${p}")
      }
    }
  }
}

def goArch() {
  // Unsupported architectures:
  //    darwin for arm64 in case isArm() needs a tweak
  //    MIPS, PPC, RISC, S390X, WASM
  if(isArm()) {
    return 'arm64'
  }
  return 'amd64'
}

def debugGoEnv() {
  // For debugging purposes only
  if (env?.PIPELINE_LOG_LEVEL?.equals('DEBUG')) {
    sh(label: "Debugging go", script: '''
      go env
      find . -name go -type f | xargs file || true
      find . -name mage -type f | xargs file || true
    ''', returnStatus: true)
  }
}
