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

  runE2E(testMatrixFile: '.ci/.fleet-server.yml')
*/
def call(Map args = [:]) {

  if (!env.JENKINS_URL?.contains('beats-ci') || !env.JENKINS_URL?.contains('fleet-ci') ) {
    error('runE2e: e2e pipeline is defined in either https://beats-ci.elastic.co/ or https://fleet-ci.elastic.co/')
  }

  // As long as elastic/beats and elastic/e2e-testing don't match each other with
  // main then it's required to do this trick
  def targetBranch = env.CHANGE_TARGET.equals('master') ? 'main' : env.CHANGE_TARGET

  def jobFolderPath = 'e2e-tests/e2e-testing-mbp'
  def jobName = args.get('jobName', isPR() ? "${targetBranch}" : "${env.JOB_BASE_NAME}")
  def gitHubCheckName = args.get('gitHubCheckName', '')
  def disableGitHubCheck =  args.get('disableGitHubCheck', false)
  def propagate = args.get('propagate', false)
  def wait = args.get('wait', false)

  if (!jobName?.trim()) {
    error('runE2E: jobName is empty.')
  }

  def e2eTestsPipeline = "${jobFolderPath}/${jobName}"

  build(job: e2eTestsPipeline,
    parameters: createParameters(args),
    propagate: propagate,
    wait: wait
  )

  if (gitHubCheckName?.trim() && !disableGitHubCheck) {
    githubNotify(context: "${gitHubCheckName}",
                description: "${gitHubCheckName} ...",
                status: 'PENDING',
                targetUrl: "${env.JENKINS_URL}search/?q=${e2eTestsPipeline.replaceAll('/','+')}")
  }
}

def createParameters(Map args = [:]) {
  def parameters = [
    booleanParam(name: 'forceSkipGitChecks', value: args.get('forceSkipGitChecks', true)),
    booleanParam(name: 'forceSkipPresubmit', value: args.get('forceSkipPresubmit', true)),
    booleanParam(name: 'notifyOnGreenBuilds', value: args.get('notifyOnGreenBuilds', !isPR())),
    booleanParam(name: 'NIGHTLY_SCENARIOS', value: args.get('nightlyScenarios', false)),
  ]

  addStringParameterIfValue(args.get('beatVersion', ''), 'BEAT_VERSION', parameters)
  addStringParameterIfValue(args.get('gitHubCheckSha1', ''), 'GITHUB_CHECK_SHA1', parameters)
  addStringParameterIfValue(args.get('gitHubCheckRepo', ''), 'GITHUB_CHECK_REPO', parameters)
  addStringParameterIfValue(args.get('gitHubCheckName', ''), 'GITHUB_CHECK_NAME', parameters)
  addStringParameterIfValue(args.get('kibanaVersion', ''), 'KIBANA_VERSION', parameters)
  addStringParameterIfValue(args.get('runTestsSuites', ''), 'runTestsSuites', parameters)
  addStringParameterIfValue(args.get('slackChannel', ''), 'SLACK_CHANNEL', parameters)
  addStringParameterIfValue(args.get('testMatrixFile', ''), 'testMatrixFile', parameters)

  return parameters
}

def addStringParameterIfValue(value, name, parameters) {
  if (parameters == null) {
    return
  }
  if (value?.trim()) {
    parameters << string(name: name, value: value)
  }
}
