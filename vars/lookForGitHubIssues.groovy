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
  Look for all the open issues that were reported as flakey tests. It returns
  a dictionary with the test-name as primary key and the github issue if any or empty otherwise.

  // Look for all the GitHub issues with label 'flakey-test' and test failures either test-foo or test-bar
  lookForGitHubIssues( flakeyList: [ 'test-foo', 'test-bar'], labelsFilter: [ 'flakey-test'])
*/
def call(Map args = [:]) {
  def list = args.get('flakeyList', [])
  def labels = args.get('labelsFilter', [])
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def output = [:]
  if (list) {
    try {
      // Filter all the issues given those labels.
      def issues = githubIssues(labels: labels, credentialsId: credentialsId)
      if (issues) {
        // for all the test failures and and github issues, let's look for the ones with
        // the test-name in the issue title
        list.each { testName ->
          issues.each { issue, data ->
            if (data.title?.contains(testName)) {
              output.put(testName, issue)
            } else {
              output.put(testName, '')
            }
          }
        }
      }
    } catch (err) {
      // no issues could be found, let's report the list of test failures without any issue details.
      list.each { output.put(it, '') }
    }
    return output
  } else {
    return output
  }
}
