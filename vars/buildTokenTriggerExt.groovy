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

Extends the step buildTokenTrigger to wait for and report the status
when running a job in a remote CI controller

buildTokenTriggerExt(credentialsId: 'sign-artifacts-job',
                     jenkinsUrl: 'https://foo-ci.acme.co',
                     job: 'sign-artifacts',
                     propagate: true,
                     wait: true)
*/
def call(Map args = [:]){
  def wait = args.get('wait', false)
  def propagate = args.get('propagate', false)
  if (!isPluginInstalled(pluginName: 'build-token-trigger')) {
    error('buildTokenTriggerExt: build-token-trigger plugin is not available')
  }

  if (wait || propagate) {
    waitUntil(initialRecurrencePeriod: 15000) {
      propagateError(args) {
        waitFor(args)
      }
    }
  } else {
    buildTokenTrigger(args)
  }
}

def propagateError(Map args = [:], Closure body) {
  try {
    body()
  } catch(err) {
    if (args.get('propagate', false)) {
      error("buildTokenTriggerExt: ${err.toString()}")
    }
  }
}

def waitFor(Map args = [:]) {
  echo 'TBD'
  return false
}
