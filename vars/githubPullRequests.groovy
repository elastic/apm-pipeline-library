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
  Look for the GitHub Pull Requests in the current project given the labels to be 
  filtered with. It returns a dictionary with the Pull Request id as primary key and
  then the title and branch values.

  githubPullRequests(labels: [ 'foo', 'bar' ])
*/
def call(Map args = [:]) {
  def labels = args.get('labels', [])
  def titleContains = args.get('titleContains', '')
  def limit = args.get('limit', 200)
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def output = [:]
  def issues
  try {
    // filter all the PRs given those labels.
    prs = gh(command: 'pr list', flags: [label: labels, limit: limit])
    if (prs?.trim()) {
      prs.split('\n').each { line ->
        def data = line.split('\t')
        def title = data[1]
        if (title.contains(titleContains)) {
          output.put("${data[0]}", [ title: data[1], branch: data[2] ])
        }
      }
    }
  } catch (err) {
    // no issues to be reported.
    log(level: 'WARN', text: "githubPullRequests: It failed but let's notify the error but keep going. gh command returned: '${issues}' with error: ${err.toString()}")
  }
  log(level: 'DEBUG', text: "githubPullRequests: output ${output}.")
  return output
}
