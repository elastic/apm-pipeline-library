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

  getBuildInfoJsonFiles(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER)
*/

def call(Map args = [:]) {
  def jobURL = args.containsKey('jobURL') ? args.jobURL : error('getBuildInfoJsonFiles: jobURL parameter is required.')
  def buildNumber = args.containsKey('buildNumber') ? args.buildNumber : error('getBuildInfoJsonFiles: buildNumber parameter is required.')
  def returnData = args.get('returnData', false)

  if(!isUnix()){
    error('getBuildInfoJsonFiles: windows is not supported yet.')
  }

  def restURLJob = getBlueoceanRestURLJob(jobURL: jobURL)
  def restURLBuild = "${restURLJob}runs/${buildNumber}"

  def scriptFile = 'generate-build-data.sh'
  def resourceContent = libraryResource("scripts/${scriptFile}")
  writeFile file: scriptFile, text: resourceContent
  sh(label: 'generate-build-data', returnStatus: true, script: """#!/bin/bash -x
    chmod 755 ${scriptFile}
    ./${scriptFile} ${restURLJob} ${restURLBuild} ${currentBuild.currentResult} ${currentBuild.duration}""")

  archiveArtifacts(allowEmptyArchive: true, artifacts: '*.json')

  if (returnData) {
    // Read files only once and if timeout then use default values
    def buildData = {}
    def changeSet = []
    def logData = ''
    def stepsErrors = []
    def testsErrors = []
    def testsSummary = []
    try {
      timeout(5) {
        buildData = readJSON(file: 'build-info.json')
        changeSet = readJSON(file: 'changeSet-info.json')
        stepsErrors = readJSON(file: 'steps-errors.json')
        testsErrors = readJSON(file: 'tests-errors.json')
        testsSummary = readJSON(file: 'tests-summary.json')
      }
    } catch(e) {
      log(level: 'WARN', text: 'It was a really slow query, so use what we got so far')
    }

    return [
      build: buildData,
      buildStatus: currentBuild.currentResult,
      changeSet: changeSet,
      stepsErrors: stepsErrors,
      testsErrors: testsErrors,
      testsSummary: testsSummary
    ]
  }
}

/**
  For backward compatibility
  @deprecated
*/
def call(jobURL, buildNumber){
  return call(jobURL: jobURL, buildNumber: buildNumber)
}
