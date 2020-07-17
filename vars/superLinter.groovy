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

  superLinter(envs: [ 'VALIDATE_GO=false' ])
*/
def call(Map args = [:]) {
  def varsEnv = args.get('envs', [])
  def failNever = args.get('failNever', false)
  def junitFlag = args.get('junit', true)
  def dockerImage = args.get('image', 'github/super-linter:latest')
  def reportFileName = 'super-linter.out'
  def output = '.super-linter'
  retryWithSleep(retries: 2, seconds: 5, backoff: true) {
    sh(label: 'Install super-linter', script: "docker pull ${dockerImage}")
  }
  envFlags = "-e RUN_LOCAL=true -e DISABLE_ERRORS=${failNever}"
  varsEnv.each {
    envFlags += " -e ${it}"
  }
  sh(label: 'Run super-linter', script: """
    docker run ${envFlags} \
      -e OUTPUT_FORMAT=tap -e OUTPUT_DETAILS=detailed -e OUTPUT_FOLDER=${output} \
      -v \$(pwd):/tmp/lint \
      -u \$(id -u):\$(id -g) \
      ${dockerImage}""")

  if(junitFlag) {
    dir("${output}") {
      convertGoTestResults(input: '*.xml', output: 'junit-report.xml')
      junit testResults: 'junit-report.xml', allowEmptyResults: true, keepLongStdio: true
    }
  }
}
