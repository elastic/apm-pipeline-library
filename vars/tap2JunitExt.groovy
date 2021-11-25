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
  Extend the tap2Junit step to support the quite specific use case
  from the Node.js where the TAP output is aggregated in one single
  file. Therefore it is required to parse the output to populate all
  the executed tests correctly. NOTE: this is nothing about the
  TAP, JUnit or CI since it's a specific characteristic of how the
  project handle the test report in one single file.

  // Use default setup
  tap2JunitExt()

  // Convert TAP files to JUnit using the suffix junit.xml
  tap2JunitExt(pattern: '*.TAP', suffix: 'junit.xml')

*/
def call(Map args = [:]) {
  def packageName = args.get('package', 'co.elastic')
  def pattern = args.get('pattern', '*.tap')
  def suffix = args.get('suffix', 'junit-report.xml')
  def nodeVersion =  args.get('nodeVersion', 'node:12-alpine')
  def failNever = args.get('failNever', false)

  def scriptFile = 'tap-to-junit-ext.sh'
  def resourceContent = libraryResource("scripts/${scriptFile}")
  writeFile file: scriptFile, text: resourceContent
  sh(label: scriptFile, script: """#!/bin/bash -x
                                    chmod 755 ${scriptFile}
                                    ./${scriptFile} '${pattern}' """)
  args.pattern = "${pattern}.ext"
  tap2Junit(args)
}
