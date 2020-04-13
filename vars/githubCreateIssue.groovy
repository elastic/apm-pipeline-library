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

def call(Map params = [:]) {
  if(!isUnix()) {
    error 'githubCreateIssue: windows is not supported yet.'
  }
  def title = params.containsKey('title') ? "-m ${params.title}" : error('githubCreateIssue: title argument is required.')
  def description = params.containsKey('description') ? """-m '${params.description}'""" : ''
  def assign = params.containsKey('assign') ? "-a ${params.assign}" : ''
  def milestone = params.containsKey('milestone') ? "-M ${params.milestone}" : ''
  def labels = params.containsKey('labels') ? "-l ${params.labels}" : ''
  def credentialsId = params.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
    sh(label: 'Create GitHub issue', script: "hub create ${title} ${description} ${assign} ${labels} ${milestone}")
  }
}
