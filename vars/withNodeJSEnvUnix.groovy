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

  withNodeJSEnvUnix(version: '14.17.5'){
    sh(label: 'Node version', script: 'node --version')
  }

*/
def call(Map args = [:], Closure body) {
  withEnv([
    "HOME=${env.WORKSPACE}"
  ]){
    def node_version = installNode(args)
    def nvmNodePath = getNodePath(node_version)
    withEnv(["PATH+NVM=${nvmNodePath}"]){
      sh(label: 'Debug withNodeJSEnvUnix installation', script: """
        which npm || true
        whereis npm || true
        npm --version || true
        which node || true
        whereis node || true
        node --version || true
        ls -l ${nvmNodePath} || true
      """)
      body()
    }
  }
}

def installNode(Map args = [:]) {
  def version = args.containsKey('version') ? args.version : nodeJSDefaultVersion()
  def nvmNodeFile = nvmNodeVersionFile()
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    sh(label: 'Installing nvm', script: '''
      set -e
      set +x
      export NVM_DIR="${HOME}/.nvm"
      [ -s "${NVM_DIR}/nvm.sh" ] && . "${NVM_DIR}/nvm.sh"

      if [ -z "$(command -v nvm)" ]; then
        rm -fr "${NVM_DIR}"
        curl -so- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
      fi
    ''')
    sh(label: "Installing Node.js ${version}", script: """
      set -e
      set +x
      export NVM_DIR="\${HOME}/.nvm"
      [ -s "\${NVM_DIR}/nvm.sh" ] && . "\${NVM_DIR}/nvm.sh"

      nvm install ${version}
      echo "Fetch nvm version"
      nvm version
      nvm version | head -n1
      nvm version | head -n1 > "${nvmNodeFile}"
    """)
  }
  return readFile(file: nvmNodeFile).trim()
}

def getNodePath(version) {
  return "${HOME}/.nvm/versions/node/${version}/bin"
}

def nvmNodeVersionFile() {
  return "${pwd(tmp: true)}/.nvm-node-version"
}
