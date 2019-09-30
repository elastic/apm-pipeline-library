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
  restURLJob = "${env.JENKINS_URL}/blue/rest/organizations/jenkins/pipelines/${restURLJob}"
  def restURLBuild = "${restURLJob}/runs/${buildNumber}"

  downloadJSONFile("${restURLJob}", "job-info.json")
  downloadJSONFile("${restURLBuild}", "build-info.json")
  downloadJSONFile("${restURLBuild}/blueTestSummary", "tests-summary.json")
  downloadJSONFile("${restURLBuild}/tests", "tests-info.json")
  downloadJSONFile("${restURLBuild}/changeSet", "changeSet-info.json")
  downloadJSONFile("${restURLBuild}/artifacts", "artifacts-info.json")
  downloadJSONFile("${restURLBuild}/steps", "steps-info.json")
  downloadJSONFile("${restURLBuild}/log", "pipeline-log.txt")

  sh(label: "Console lg sumary", script: "tail -n 100 pipeline-log.txt > pipeline-log-summary.txt")

  def json = [:]
  json.job = readJSON(file: "job-info.json")
  json.build = readJSON(file: "build-info.json")
  json.test_summary = readJSON(file: "tests-summary.json")
  json.test = readJSON(file: "tests-info.json")
  json.changeSet = readJSON(file: "changeSet-info.json")
  json.artifacts = readJSON(file: "artifacts-info.json")
  json.steps = readJSON(file: "steps-info.json")
  json.log = readFile(file: "pipeline-log.txt")

  /** The build is not finished so we have to fix some values */
  json.build.result = currentBuild.currentResult
  json.build.state = "FINISHED"
  json.build.durationInMillis = currentBuild.duration
  writeJSON(file: "build-info.json" , json: toJSON(json.build), pretty: 2)

  writeJSON(file: "build-report.json" , json: toJSON(json), pretty: 2)
}

def downloadJSONFile(url, file){
  def ret = sh(label: "Get Build info ${file}", script: "curl -sfSL --max-time 60 --connect-timeout 10 -o ${file} ${url}", returnStatus: true)

  if(ret != 0){
    writeJSON(file: "${file}" , json: toJSON("{}"), pretty: 2)
  }
}
