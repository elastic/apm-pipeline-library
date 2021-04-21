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
  Checks if the given Docker image exists.

  whenTrue(dockerImageExists(image: 'hello-world:latest')) {
    ...
  }
*/
def call(Map args = [:]) {
  def image = args.containsKey('image') ? args.image : error('dockerImageExists: image parameter is required')

  if (!isInstalled(tool: 'docker', flag: '--version')) {
    error 'dockerImageExists: Docker is not installed'
  }

  if (cmd(returnStatus: true, script: "docker inspect -f \"{{.Id}}\" ${image}") == 0) {
    log(level: 'DEBUG', text: "${image} exists in the Docker host")
    return true
  }

  def redirectStdout = isUnix() ? '>/dev/null' : '>NUL'
  log(level: 'DEBUG', text: "${image} does not exist in the Docker host: checking registry")
  if (cmd(returnStatus: true, script: "docker manifest inspect ${image} ${redirectStdout}") == 0) {
    log(level: 'DEBUG', text: "${image} exists in the Docker registry")
    return true
  }

  log(level: 'DEBUG', text: "${image} does not exist at all")
  return false
}
