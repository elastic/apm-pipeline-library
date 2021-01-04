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
  Configure the hub app to run the body closure.
  
  withHubCredentials(credentialsId: 'credential') {
    // block
  }

*/
def call(Map args = [:], Closure body) {
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
  if(!isUnix()){
    error('withHubCredentials: windows is not supported yet.')
  }

  withCredentials([usernamePassword(credentialsId: "${args.credentialsId}",
                                    passwordVariable: 'GITHUB_TOKEN',
                                    usernameVariable: 'GITHUB_USER')]) {
    try {
      dir("${env.HOME}/.config") {
        writeFile(file: 'hub', text: """
github.com:
- user: ${GITHUB_USER}
  oauth_token: ${GITHUB_TOKEN}
  protocol: https""")
      }
      body()
    } catch(e) {
      error "withHubCredentials: error ${e}"
    } finally {
      dir("${env.HOME}/.config") {
        deleteDir()
      }
    }
  }
}
