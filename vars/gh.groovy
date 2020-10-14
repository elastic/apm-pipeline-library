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
  Wrapper to interact with the gh command line. It returns the stdout output.

  // List all the open issues with the label 
  gh(command: 'issue list', flags: [ label: ['flaky-test'], state: 'open' ])

  // Create issue with title and body
  gh(command: 'issue create', flags: [ title: "I found a bug", body: "Nothing works" ])

*/

def call(Map args = [:]) {
  if(!isUnix()) {
    error 'gh: windows is not supported yet.'
  }
  def command = args.containsKey('command') ? args.command : error('gh: command argument is required.')
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def flags = args.get('flags', [])
  withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
    def flagsCommand = ''
    if (flags) {
      flags.each { k, v ->
        if (v instanceof List) {
          v.each { value ->
            flagsCommand += "--${k}=${value} "
          }
        } else {
          flagsCommand += "--${k}=${v} "
        }
      }
    }
    def output = sh(label: "gh ${command}", script: "gh ${command} ${flagsCommand}", returnStdout: true)
    return output
  }
}
