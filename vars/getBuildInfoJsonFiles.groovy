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
  Grab build related info from the Blueocean REST API and store it on JSON files.
  Then put all togeder in a simple JSON file.

  getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
*/
def call(jobURL, buildNumber){
  if(!isUnix()){
    error('getBuildInfoJsonFiles: windows is not supported yet.')
  }
  def restURLJob = "${jobURL}" - "${env.JENKINS_URL}job/"
  restURLJob = restURLJob.replace("/job/","/")
  restURLJob = "${env.JENKINS_URL}blue/rest/organizations/jenkins/pipelines/${restURLJob}"
  if (!restURLJob.endsWith('/')) {
    restURLJob += '/'
  }
  def restURLBuild = "${restURLJob}runs/${buildNumber}"

  def scriptFile = 'generate-build-data.sh'
  if (!fileExists(scriptFile)) {
    def resourceContent = libraryResource('scripts/generate-build-data.sh')
    writeFile file: scriptFile, text: resourceContent
  }
  sh(label: 'generate-build-data', returnStatus: true, script: """#!/bin/bash
    chmod 755 generate-build-data.sh
    ./generate-build-data.sh ${restURLJob} ${restURLBuild} ${currentBuild.currentResult} ${currentBuild.duration}
    """)
}
