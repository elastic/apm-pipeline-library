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
  List the GitHub releases in the current project. It returns
  a dictionary with the release id as primary key and then the whole information.

  listGithubReleases()
*/
def call(Map args = [:]) {
  def limit = args.get('limit', 200)
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def output = [:]
  def releases
  try {
    // filter all the issues given those labels.
    releases = gh(command: 'release list', flags: [limit: limit])
    if (releases?.trim()) {
      releases.split('\n').each { line ->
        def data = line.split(' ')
        output.put("${data[0]}", [ value: line ])
      }
    }
  } catch (err) {
    log(level: 'WARN', text: "githubReleases: It failed but let's notify the error but keep going. gh command returned: '${releases}' with error: ${err.toString()}")
  }
  log(level: 'DEBUG', text: "githubReleases: output ${output}.")
  return output
}
