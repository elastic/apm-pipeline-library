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
  Login to hub.docker.com with an authentication credentials from a Vault secret.
  The vault secret contains `user` and `password` fields with the authentication details.

  dockerLogin(secret: 'secret/team/ci/secret-name')
  dockerLogin(secret: 'secret/team/ci/secret-name', registry: "docker.io")
*/
def call(Map params = [:]){
  def secret = params.containsKey('secret') ? params.secret : error("dockerLogin: No valid secret to looking for.")
  def registry = params.containsKey('registry') ? params.registry : "docker.io"

  def jsonValue = getVaultSecret(secret: secret)
  def data = jsonValue.containsKey('data') ? jsonValue.data : error("dockerLogin: No valid data in secret.")
  def dockerUser = data.containsKey('user') ? data.user : error("dockerLogin: No valid user in secret.")
  def dockerPassword = data.containsKey('password') ? data.password : error("dockerLogin: No valid password in secret.")

  wrap([$class: 'MaskPasswordsBuildWrapper',
    varPasswordPairs: [
      [var: 'DOCKER_USER', password: dockerUser],
      [var: 'DOCKER_PASSWORD', password: dockerPassword],
  ]]) {
    withEnv([
      "DOCKER_USER=${dockerUser}",
      "DOCKER_PASSWORD=${dockerPassword}"
    ]) {
      // When running in the CI with multiple parallel stages
      // the access could be considered as a DDOS attack.
      retryWithSleep(retries: 3, seconds: 5, exponential: true) {
        if (isUnix()) {
          sh(label: "Docker login", script: """
            set +x
            if command -v host; then
              host ${registry} 2>&1 > /dev/null 
            fi
            if command -v dig; then
              dig ${registry} 2>&1 > /dev/null 
            fi
            docker login -u "\${DOCKER_USER}" -p "\${DOCKER_PASSWORD}" "${registry}" 2>/dev/null
            """)
        } else {
          bat(label: 'is registry service up?', script: """@ECHO OFF
            nslookup ${registry} > NUL 2>&1
          """)
          bat(label: 'Docker Login', script: """@ECHO OFF
            docker login -u "%DOCKER_USER%" -p "%DOCKER_PASSWORD%" "${registry}" 2> NUL
          """)
        }
      }
    }
  }
}
