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
  Look for all the open issues given some filters.

  For backward compatibilities the default behaviour uses the flaky tests. It returns
  a dictionary with the test-name as primary key and the github issue if any or empty otherwise.

  // Look for all the open GitHub issues with label 'flaky-test' and test failures either test-foo or test-bar
  lookForGitHubIssues(flakyList: [ 'test-foo', 'test-bar'], labelsFilter: [ 'flaky-test'])

  // Look for all the open GitHub issues with label 'automation' and the title contains 'bump: stack'
  lookForGitHubIssues(flakySearch: false, labelsFilter: ['automation'], titleContains: 'bump: stack')
*/
def call(Map args = [:]) {
  def flakySearch = args.get('flakySearch', true)
  def flakyList = args.get('flakyList', [])
  def labels = args.get('labelsFilter', [])
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def issues = [:]
  try {
    // Filter all the issues given those labels.
    issues = githubIssues(labels: labels, credentialsId: credentialsId)
  } catch(err) {
    issues = [:]
  } finally {
    if (flakySearch) {
      return searchFlakyIssues(flakyList: flakyList, issues: issues)
    } else {
      return searchIssuesWithFilters(issues: issues, titleContains: args.get('titleContains', ''))
    }
  }
}

def searchIssuesWithFilters(Map args = [:]) {
  def titleContains = args.titleContains
  def issues = args.issues
  def output = [:]
  try {
    output = issues?.findAll { issue, data -> data.title?.contains(titleContains) }
  } catch (err) {
    log(level: 'DEBUG', text: "search: failed with error '${err.toString()}'")
  }
  log(level: 'DEBUG', text: "search: output ${output}.")
  return output
}

def searchFlakyIssues(Map args = [:]) {
  def flakyList = args.flakyList
  def issues = args.issues
  if (flakyList) {
    def output = [:]
    if (issues) {
      // for all the test failures and and github issues, let's look for the ones with
      // the test-name in the issue title
      flakyList.each { testName ->
        def issue = issues.find { issue, data -> data.title?.contains(testName) }
        if(issue) {
          log(level: 'DEBUG', text: "flakySearch: issue ${issue.key} matches ${testName}.")
          output[testName] = issue.key
        } else {
          log(level: 'DEBUG', text: "flakySearch: no match for ${testName}.")
          output[testName] = ''
        }
      }
    } else {
      // no issues could be found, let's report the list of test failures without any issue details.
      flakyList.each { output.put(it, '') }
    }
    log(level: 'DEBUG', text: "flakySearch: output ${output}.")
    return output
  } else {
    log(level: 'WARN', text: "flakySearch: flakyList is empty.")
    return [:]
  }
}
