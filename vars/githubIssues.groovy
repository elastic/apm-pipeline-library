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
  Look for the GitHub issues in the current project given the labels to be filtered with. It returns
  a dictionary with the issue id as primary key and then the state and title values.

  githubIssues(labels: [ 'foo', 'bar' ])
*/
def call(Map args = [:]) {
  def labels = args.get('labels', [])
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def output = [:]
  try {
    // filter all the issues given those labels.
    def issues = gh(command: 'issue list', flags: [ label: labels, limit: 200])
    if (issues?.trim()) {
      issues.split('\n').each { line ->
        def data = line.split('\t')
        output.put(data[0], [ state: data[1], title: data[2] ])
      }
    }
  } catch (err) {
    // no issues to be reported.
    log(level: 'WARN', text: "githubIssues: It failed but let's notify the error but keep going. ${err.toString()}")
  }
  return output
}
