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

Helper function that implements https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API

  buildInController(job: 'foo',
                    parameters: [string(name: "my param", value: some_value)]
                    controller: 'my-new-controller')
*/
def call(Map args = [:]){
  def job = args.job
  def parameters = args.parameters
  def wait = args.get('wait', true)
  def propagate = args.get('propagate', true)
  def quietPeriod = args.get('quietPeriod', 1)
  def controller = args.containsKey('controller') ? args.controller : error('')

  // Do we want to validate the Job is in the approval list of jobs?
  // Do we want to validate the controller is in the approval list of controllers?

  // Given the controller then the credentials in Vault can match the secret name
}
