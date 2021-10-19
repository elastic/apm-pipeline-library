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

  if (!env.JENKINS_URL?.contains('beats-ci')) {
    error('runE2e: e2e pipeline is defined in https://beats-ci.elastic.co/')
  }

  def jobName = args.get('jobName', 'e2e-tests/e2e-testing-mbp')
  def fullJobName = args.get('fullJobName', '')
  def gitHubCheckName = args.get('gitHubCheckName', '')
  def propagate = args.get('propagate', false)
  def wait = args.get('wait', false)

  if (jobName?.trim() && fullJobName?.trim()) {
    log(level: 'WARNING', text: 'runE2E: jobName amd fullJobName are set. fullJobName param got precedency instead.')
  }

  def e2eTestsPipeline = (fullJobName?.trim()) ? fullJobName : "${jobName}/${isPR() ? "${env.CHANGE_TARGET}" : "${env.JOB_BASE_NAME}"}"

  build(job: "${e2eTestsPipeline}",
    parameters: createParameters(args),
    propagate: propagate,
    wait: wait
  )

  if (gitHubCheckName?.trim()) {
    githubNotify(context: "${gitHubCheckName}",
                description: "${gitHubCheckName} ...",
                status: 'PENDING',
                targetUrl: "${env.JENKINS_URL}search/?q=${e2eTestsPipeline.replaceAll('/','+')}")
  }
}

def createParameters(Map args = [:]) {
  def beatVersion = args.get('beatVersion', '')
  def gitHubCheckName = args.get('gitHubCheckName', '')
  def gitHubCheckRepo = args.get('gitHubCheckRepo', '')
  def gitHubCheckSha1 = args.get('gitHubCheckSha1', '')
  def notifyOnGreenBuilds = args.get('notifyOnGreenBuilds', !isPR())
  def forceSkipGitChecks = args.get('forceSkipGitChecks', true)
  def forceSkipPresubmit = args.get('forceSkipPresubmit', true)
  def runTestsSuites = args.get('runTestsSuites', '')
  def slackChannel = args.get('slackChannel', '')
  def testMatrixFile = args.get('testMatrixFile', '')

  def parameters = [
    booleanParam(name: 'forceSkipGitChecks', value: forceSkipGitChecks),
    booleanParam(name: 'forceSkipPresubmit', value: forceSkipPresubmit),
    booleanParam(name: 'notifyOnGreenBuilds', value: notifyOnGreenBuilds),
  ]

  addStringParameterIfKey(beatVersion, 'BEAT_VERSION', parameters)
  addStringParameterIfKey(gitHubCheckSha1, 'GITHUB_CHECK_SHA1', parameters)
  addStringParameterIfKey(gitHubCheckRepo, 'GITHUB_CHECK_REPO', parameters)
  addStringParameterIfKey(gitHubCheckName, 'GITHUB_CHECK_NAME', parameters)
  addStringParameterIfKey(runTestsSuites, 'runTestsSuites', parameters)
  addStringParameterIfKey(slackChannel, 'slackChannel', parameters)
  addStringParameterIfKey(testMatrixFile, 'testMatrixFile', parameters)

  return parameters
}

def addStringParameterIfKey(arg, name, parameters) {
  if (parameters == null) {
    return
  }
  if (arg?.trim()) {
    parameters << string(name: name, value: arg)
  }
}
