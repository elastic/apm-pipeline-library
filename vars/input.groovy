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

  As long as we cannot control the abort then let's use this approach.

  It will create a new env variable called INPUT_ABORTED and will be set to true if either the user
  or a timeout happens, otherwise it will be set to false.

  Further details: https://brokenco.de/2017/08/03/overriding-builtin-steps-pipeline.html
*/
def call(params) {
  log(level: 'INFO', text: 'Override default input')
  def userInput = true
  def didTimeout = false
  try {
    steps.input(params)
  } catch(err) {
    log(level: 'DEBUG', text: "input: catch error ${err}")
    // See https://support.cloudbees.com/hc/en-us/articles/230922148-Pipeline-How-to-add-an-input-step-with-timeout-that-continues-if-timeout-is-reached-using-a-default-value
    def user = err.getCauses()[0]?.getUser()
    if ('SYSTEM' == user?.toString()) { // SYSTEM means timeout.
      didTimeout = true
    } else {
      userInput = false
      log(level: 'DEBUG', text: "Aborted by: [${user}]")
    }
  } finally {
    env.INPUT_ABORTED = didTimeout || !userInput
  }
}
