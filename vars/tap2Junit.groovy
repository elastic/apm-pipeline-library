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
  if(!isUnix()) {
    error 'tap2Junit: windows is not supported yet.'
  }
  def packageName = args.get('package', 'co.elastic')
  def pattern = args.get('pattern', '*.tap')
  def suffix = args.get('suffix', 'junit-report.xml')
  def nodeVersion =  args.get('nodeVersion', 'node:12-alpine')
  def failNever = args.get('failNever', false)
  def archiveJunit = args.get('archiveJunit', false)

  def scriptFile = 'tap-to-junit.sh'
  def resourceContent = libraryResource("scripts/${scriptFile}")
  writeFile file: scriptFile, text: resourceContent
  sh(label: scriptFile,
     returnStatus: failNever,
     script: """#!/bin/bash
        chmod 755 ${scriptFile}
        ./${scriptFile} '${pattern}' '${packageName}' '${suffix}' '${nodeVersion}'
  """)

  if (archiveJunit) {
    archiveArtifacts(allowEmptyArchive: true, artifacts: "*-${suffix}")
  }
  junit(testResults: "*-${suffix}", allowEmptyResults: true, keepLongStdio: true)
}
