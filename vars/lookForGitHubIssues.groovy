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
  Look for all the open issues that were reported as flaky tests. It returns
  a dictionary with the test-name as primary key and the github issue if any or empty otherwise.

  // Look for all the GitHub issues with label 'flaky-test' and test failures either test-foo or test-bar
  lookForGitHubIssues( flakyList: [ 'test-foo', 'test-bar'], labelsFilter: [ 'flaky-test'])
*/
def call(Map args = [:]) {
  def flakyList = args.get('flakyList', [])
  def labels = args.get('labelsFilter', [])
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def output = [:]
  if (flakyList) {
    try {
      // Filter all the issues given those labels.
      def issues = githubIssues(labels: labels, credentialsId: credentialsId)
      if (issues) {
        // for all the test failures and and github issues, let's look for the ones with
        // the test-name in the issue title
        flakyList.each { testName ->
          def issue = issues.find { issue, data -> data.title?.contains(testName) }
          if(issue) {
            log(level: 'DEBUG', text: "lookForGitHubIssues: issue ${issue.key} matches ${testName}.")
            output[testName] = issue.key
          } else {
            log(level: 'DEBUG', text: "lookForGitHubIssues: no match for ${testName}.")
            output[testName] = ''
          }
        }
      }
    } catch (err) {
      // no issues could be found, let's report the list of test failures without any issue details.
      flakyList.each { output.put(it, '') }
    }
    log(level: 'DEBUG', text: "lookForGitHubIssues: output ${output}.")
    return output
  } else {
    log(level: 'WARN', text: "lookForGitHubIssues: flakyList is empty.")
    return output
  }
}
