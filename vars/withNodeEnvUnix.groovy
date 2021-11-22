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
 Install Node.js with NVM and run some command in a pre-configured environment for Unix.

  withNodeEnvUnix(version: '14.17.5'){
    sh(label: 'Node version', script: 'node --version')
  }

*/
def call(Map args = [:], Closure body) {
  withEnv([
    "HOME=${env.WORKSPACE}"
  ]){
    def node_version = installNode(args)
    withEnv(["PATH+NVM=${HOME}/.nvm/versions/node/${node_version}/bin"]){
      body()
    }
  }
}

def installNode(Map args = [:]) {
  def version = args.containsKey('version') ? args.version : nodeDefaultVersion()
  def nodeVersionLocation = pwd(tmp: true)
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    sh(label: 'Installing nvm', script: '''
      set -e
      export NVM_DIR="${HOME}/.nvm"
      [ -s "${NVM_DIR}/nvm.sh" ] && . "${NVM_DIR}/nvm.sh"

      if [ -z "$(command -v nvm)" ]; then
        rm -fr "${NVM_DIR}"
        curl -so- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
      fi
    ''')
    sh(label: "Installing Node.js ${version}", script: """
      set -e
      export NVM_DIR="\${HOME}/.nvm"
      [ -s "\${NVM_DIR}/nvm.sh" ] && . "\${NVM_DIR}/nvm.sh"

      nvm install ${version}
      nvm version | head -n1 > "${nodeVersionLocation}/.nvm-node-version"
    """)
  }

  return readFile(file: "${nodeVersionLocation}/.nvm-node-version").trim()
}
