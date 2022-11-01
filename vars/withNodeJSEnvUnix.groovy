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
      if (!node_version.trim()) {
        error('withNodeJSEnvUnix: the node version could not be found! Look for errors in the logs.')
      }
      body()
    }
  }
}

def installNode(Map args = [:]) {
  def version = args.containsKey('version') ? args.version : nodeJSDefaultVersion()
  def nvmVersion = args.get('nvmVersion', 'v0.39.2')
  def nvmNodeFile = nvmNodeVersionFile()
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    sh(label: 'Installing nvm', script: """
      set -e
      set +x
      export NVM_DIR="\${HOME}/.nvm"
      if [ -s "\${NVM_DIR}/nvm.sh" ] ; then
        echo "load existing nvm environment"
        . "\${NVM_DIR}/nvm.sh"
      fi

      if [ -z "\$(command -v nvm)" ]; then
        echo "installing nvm"
        rm -fr "\${NVM_DIR}"
        curl -so- https://raw.githubusercontent.com/nvm-sh/nvm/${nvmVersion}/install.sh | bash
      fi
    """)
    sh(label: "Installing Node.js ${version}", script: """
      set -e
      set +x
      echo "load existing nvm environment"
      export NVM_DIR="\${HOME}/.nvm"
      [ -s "\${NVM_DIR}/nvm.sh" ] && . "\${NVM_DIR}/nvm.sh"

      echo "install node.js"
      nvm install --no-progress --default ${version}
      nvm use ${version}

      echo "Debug nvm env"
      nvm ls
      nvm version
      nvm --version
      nvm current
      nvm version | head -n1

      echo "Fetch the default nodejs version installed with nvm"
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
