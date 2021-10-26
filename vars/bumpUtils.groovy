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
  Utils class for the bump automation pipelines
*/

def areChangesToBePushed(String branch) {
  return sh(returnStatus: true, script: "git diff --quiet HEAD..${branch}") != 0
}

def createBranch(Map args = [:]) {
  sh(script: """git checkout -b "${args.prefix}-\$(date "+%Y%m%d%H%M%S")-${args.suffix}" """, label: 'Git branch creation')
}

def isVersionAvailable(stackVersion) {
  // pinned snapshot versions use -SNAPSHOT suffix.
  def version = stackVersion.endsWith('SNAPSHOT') ? stackVersion : "${stackVersion}-SNAPSHOT"
  return dockerImageExists(image: "docker.elastic.co/elasticsearch/elasticsearch:${version}")
}

def prepareContext(Map args = [:]) {
  deleteDir()
  setupAPMGitEmail(global: true)
  git(url: "https://github.com/${args.org}/${args.repo}.git",
      branch: args.branchName,
      credentialsId: args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'))
}

def getCurrentReleaseFor7Minor() {
  def stackVersions = readProperties(text: libraryResource('versions/releases.properties'))
  def version = stackVersions['current_7']
  if (!version?.trim()) {
    error('getCurrentReleaseFor7Minor: "current_7" has got an empty value')
  }
  return version
}
