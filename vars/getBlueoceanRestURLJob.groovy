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
  Given the jobURL and the build number then returns its BlueOcean REST URL

  getBlueoceanRestURLJob(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER)
*/

def call(Map args = [:]) {
  def jobURL = args.containsKey('jobURL') ? args.jobURL : error('getBlueoceanRestURLJob: jobURL parameter is required.')
  def buildNumber = args.containsKey('buildNumber') ? args.buildNumber : error('getBlueoceanRestURLJob: buildNumber parameter is required.')
  def restURLJob = "${jobURL}" - "${env.JENKINS_URL}job/"
  restURLJob = restURLJob.replace("/job/","/")
  restURLJob = "${env.JENKINS_URL}blue/rest/organizations/jenkins/pipelines/${restURLJob}"
  if (!restURLJob.endsWith('/')) {
    restURLJob += '/'
  }
  return restURLJob
}
