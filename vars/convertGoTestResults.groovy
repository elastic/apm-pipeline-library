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
  Converts the Go test result output to JUnit result file

  sh(label: 'Run test', script: 'go test -v ./...|tee unit-report.txt')
  convertGoTestResults(input: 'unit-report.txt', output: 'junit-report.xml')

**/

def call(Map args = [:]) {
  def testsReport = args.containsKey('input') ? args.input : error('convertGoTestResults: missing input file')
  def junitReport = args.containsKey('output') ? args.output : error('convertGoTestResults: missing output file')
  withMageEnv(){
    sh(
      label: 'Convert test results to JUnit',
      script: "go-junit-report > ${junitReport} < ${testsReport}"
    )
  }
  junit(allowEmptyResults: true,
    keepLongStdio: true,
    testResults: "${junitReport}")
}
