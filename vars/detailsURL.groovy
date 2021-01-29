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
  Wrap the GitHub notify check step

  withGithubCheck(context: 'checkName', description: 'Execute something') {
    // block
  }

  withGithubCheck(context: 'checkName', description: 'Execute something', isBlueOcean: false) {
    // block
  }

*/
def call(Map args = [:]) {
  def tab = args.get('tab', 'pipeline')
  def isBlueOcean = args.get('isBlueOcean', false)
  // Use the https
  if (tab.startsWith('http')) {
    return tab
  }
  // If pipeline then let's point to the BLueOcean Stage URL
  if (tab.equals('pipeline')) {
    def stageId = getStageId()
    if (stageId) {
      def restURLJob = getBlueoceanRestURLJob(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER)
      return "${restURLJob}runs/${env.BUILD_NUMBER}/nodes/${stageId}log/?start=0"
    }
    return null
  }

  // Get the URL for the given tab.
  if (isBlueOcean) {
    return getBlueoceanTabURL(tab)
  }
  return getTraditionalPageURL(tab)
}
