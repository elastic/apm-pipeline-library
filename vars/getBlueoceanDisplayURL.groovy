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

  Provides the Blueocean URL for the current build/run

  Blueocean doesn't provide any env variable with the final URL to the BO view.
  Besides, folder separator is raw encoded, aka %2F, rather than '/'. Therefore,
  it's not easy to detect programmatically what items are either MBP or folders.

  Further details: https://groups.google.com/forum/#!topic/jenkinsci-users/-fuk4BK6Hvs

  def URL = getBlueoceanDisplayURL()
*/
def call() {
  def jobName = env.JOB_NAME.replace("/","%2F")
  def url = env.JENKINS_URL.endsWith('/') ? env.JENKINS_URL : "${env.JENKINS_URL}/"
  return "${url}blue/organizations/jenkins/${jobName}/detail/${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}/"
}
