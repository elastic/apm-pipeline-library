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
  Trigger the end 2 end testing job.

  runE2e(jobName: 'foo', testMatrixFile: '.ci/.fleet-server.yml')
*/
def call(Map args = [:]) {
  def jobName = args.get('jobName')
  def testMatrixFile = args.get('testMatrixFile', '')
  def gitHubCheckName = args.get('gitHubCheckName')
  def notifyOnGreenBuilds = args.get('notifyOnGreenBuilds', !isPR())
  def beatVersion = args.get('beatVersion')

  def e2eTestsPipeline = "${jobName}/${isPR() ? "${env.CHANGE_TARGET}" : "${env.JOB_BASE_NAME}"}"

  def parameters = [
    booleanParam(name: 'forceSkipGitChecks', value: true),
    booleanParam(name: 'forceSkipPresubmit', value: true),
    booleanParam(name: 'notifyOnGreenBuilds', value: notifyOnGreenBuilds),
    string(name: 'BEAT_VERSION', value: beatVersion),
    string(name: 'testMatrixFile', value: testMatrixFile),
    string(name: 'GITHUB_CHECK_NAME', value: gitHubCheckName),
    string(name: 'GITHUB_CHECK_REPO', value: env.REPO),
    string(name: 'GITHUB_CHECK_SHA1', value: env.GIT_BASE_COMMIT),
  ]

  build(job: "${e2eTestsPipeline}",
    parameters: parameters,
    propagate: false,
    wait: false
  )

  githubNotify(context: "${gitHubCheckName}", description: "${gitHubCheckName} ...", status: 'PENDING', targetUrl: "${env.JENKINS_URL}search/?q=${e2eTestsPipeline.replaceAll('/','+')}")
}
