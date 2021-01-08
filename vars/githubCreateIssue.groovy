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
  Create an Issue in GitHub as long as the command runs in the git repo.

  githubCreateIssue(title: 'Foo')

*/

def call(Map args = [:]) {
  if(!isUnix()) {
    error 'githubCreateIssue: windows is not supported yet.'
  }
  def assignee = args.get('assign', '')
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def description = args.get('description', '')
  def labels = args.get('labels', '')
  def milestone = args.get('milestone', '')
  def title = args.containsKey('title') ? args.title : error('githubCreateIssue: title argument is required.')
  withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
    def value = gh(command: 'issue create', flags: [ assignee: assignee,
                                                     label: labels,
                                                     milestone: milestone,
                                                     title: title,
                                                     body: description])
    return value
  }
}
