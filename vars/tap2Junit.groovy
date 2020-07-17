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
  Transform the TAP to JUnit, for such it uses some parameters
  to customise the generated output.

  // Use default setup
  tap2Junit()

  // Convert TAP files to JUnit using the suffix junit.xml
  tap2Junit(pattern: '*.TAP', suffix: 'junit.xml')

*/
def call(Map args = [:]) {
  def packageName = args.get('package', 'co.elastic')
  def pattern = args.get('pattern', '*.tap')
  def suffix = args.get('suffix', 'junit-report.xml')
  def nodeVersion =  args.get('nodeVersion', 'node:12-alpine')
  def failNever = args.get('failNever', false)
  sh(label: 'TAP to JUnit',
    returnStatus: failNever,
    script: """
      docker run --rm \
        -v \$(pwd):/usr/src/app \
        -w /usr/src/app \
        -u \$(id -u):\$(id -g) \
        ${nodeVersion} \
        sh -c 'export HOME=/tmp ; mkdir ~/.npm-global; npm config set prefix '~/.npm-global' ; npm install tap-xunit -g ; for i in ${pattern}; do cat \${i} | /tmp/.npm-global/bin/tap-xunit --package='${packageName}' > \${i%.*}-${suffix} ; done'
    """)
  junit testResults: "*-${suffix}", allowEmptyResults: true, keepLongStdio: true
}
