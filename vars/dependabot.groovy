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
  Run dependabot and create PRs for the given project using the credentials.

  // Run dependabot for the apm-agent-java repo and assign those PRs to the v1v user
  dependabot(project: 'elastic/apm-agent-java', assign: 'v1v', package: 'maven')

*/
def call(Map params = [:]){
  if(!isUnix()) {
    error 'dependabot: windows is not supported yet.'
  }
  def project = params.containsKey('project') ? "${params.project}" : error('dependabot: project argument is required. Format org/repo')
  def packageManager = params.containsKey('package') ? "${params.package}" : error('dependabot: package argument is required.')
  def assign = params.containsKey('assign') ? "${params.assign}" : ''
  def credentialsId = params.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def dockerImage = params.get('image', 'docker.elastic.co/observability-ci/dependabot')
  def registry = params.get('registry', 'docker.elastic.co')
  def secretRegistry = params.get('secretRegistry', 'secret/apm-team/ci/docker-registry/prod')

  withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
    if (registry && secretRegistry) {
      dockerLogin(secret: "${secretRegistry}", registry: "${registry}")
    }
    sh(label: 'Run dependabot', script: """
      docker run -t --rm \
      -e GITHUB_ACCESS_TOKEN=${env.GITHUB_TOKEN} \
      -e PROJECT_PATH=${project} \
      -e PACKAGE_MANAGER=${packageManager} \
      -e PULL_REQUESTS_ASSIGNEE=${assign} \
      ${dockerImage}
    """)
  }
}
