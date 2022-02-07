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
 Install Kind, Kubectl and configure Kind to run some command within the kind/kubectl context

  withKindEnv(k8sVersion: 'v1.23.0', kindVersion: 'v0.11.1'){
    ..
  }
**/

def call(Map args = [:], Closure body) {
  if(!isUnix()){
    error('withKindEnv: windows is not supported yet.')
  }
  def k8sVersion = args.get('k8sVersion', 'v1.23.0')
  def kindVersion = args.get('kindVersion', 'v0.11.1')

  withEnv(["K8S_VERSION=${k8sVersion}", "KIND_VERSION=${kindVersion}", "KUBECONFIG=${env.WORKSPACE}/kubecfg"]){
    installKind()
    installKubectl()
    try {
      setupKind()
      body()
    } finally {
      sh(label: 'Delete cluster', script: 'kind delete cluster')
    }
  }
}

def setupKind(Map args = [:]) {
  def i = 0
  // See https://github.com/elastic/beats/pull/21857
  // Add some environmental resilience when setup does not work the very first time.
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    try {
      sh(label: "Setup kind", script: libraryResource('scripts/install/kind-setup.sh'))
    } catch(err) {
      i++
      sh(label: 'Delete cluster', script: 'kind delete cluster')
      if (i > 2) {
        error("Setup kind failed with error '${err.toString()}'")
      }
    }
  }
}

def installKind(Map args = [:]) {
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    sh(label: "Install kind", script: libraryResource('scripts/install/kind.sh'))
  }
}

def installKubectl(Map args = [:]) {
  retryWithSleep(retries: 3, seconds: 5, backoff: true){
    sh(label: "Install kubectl", script: libraryResource('scripts/install/kubectl.sh'))
  }
}
