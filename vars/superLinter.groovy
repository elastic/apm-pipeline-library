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
  Run the github/superLinter

  superLinter()

  superLinter(envs: [ 'VALIDATE_GO=false', 'DISABLE_ERRORS=false' ])
*/
def call(Map args = [:]) {
  def varsEnv = args.get('envs', [])
  def failNever = args.get('failNever', false)
  def dockerImage = 'github/super-linter:latest'
  def reportFileName = 'super-linter.out'
  retryWithSleep(retries: 2, seconds: 5, backoff: true) {
    sh(label: 'Install super-linter', script: "docker pull ${dockerImage}")
  }
  envFlags = "-e RUN_LOCAL=true -e DISABLE_ERRORS=${failNever}"
  varsEnv.each {
    envFlags << " -e ${it}"
  }
  sh(label: 'Run super-linter', script: "docker run ${envFlags} -v \$(pwd):/tmp/lint ${dockerImage}")
}
