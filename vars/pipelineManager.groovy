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
  This step adds certains validations which might be required to be done per build, for such it does
  use other steps.

  pipelineManager([ cancelPreviousRunningBuilds: [ when: 'PR', params: [ maxBuildsToSearch: 5 ] ],
                    firstTimeContributor: [ when: 'ALWAYS' ] ])

  // TODO
  -  ManagerFactory
  -  The order is important in the map
*/

def call(Map args = [:]) {
  def cancel = args.get('cancelPreviousRunningBuilds', null)
  def firstTime = args.get('firstTimeContributor', null)
  def apmTraces = args.get('apmTraces', null)

  if (cancel) {
    def when = cancel.get('when', 'always')
    if (isEnabled(when)) {
      cancelPreviousRunningBuilds(cancel.get('params', [:]))
    } else {
      log(level: 'DEBUG', text: "cancelPreviousRunningBuilds step is not enabled for '${when}'")
    }
  }
  if (firstTime) {
    log(level: 'INFO', text: 'firstTimeContributor step is not available yet.')
  }
  if(apmTraces && isEnabled(apmTraces.get('when', 'always'))) {
    log(level: 'INFO', text: 'apmTraces is enabled.')
    setEnvVar('APM_CLI_SERVICE_NAME',"${env.JOB_NAME}")
    apmCli(transactionName: "Pipeline", saveTsID: true)
  }
}

def isEnabled(String when) {
  def isEnabled = false

  switch(when) {
    case 'ALWAYS':
      isEnabled = true
      break
    case 'BRANCH':
      isEnabled = !isPR()
      break
    case 'TAG':
      isEnabled = env.TAG_NAME?.trim() ? true : false
      break
    case 'PR':
      isEnabled = isPR()
      break
    default:
      isEnabled = false
  }
  return isEnabled
}
