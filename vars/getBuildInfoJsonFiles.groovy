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
  def restURLBuild = "${restURLJob}runs/${buildNumber}"

  bulkDownload(["${restURLJob}": 'job-info.json',
                "${restURLBuild}/": 'build-info.json',
                "${restURLBuild}/blueTestSummary/": 'tests-summary.json',
                "${restURLBuild}/tests?limit=100000000": 'tests-info.json',
                "${restURLBuild}/changeSet/": 'changeSet-info.json',
                "${restURLBuild}/artifacts/": 'artifacts-info.json',
                "${restURLBuild}/steps/": 'steps-info.json',
                "${restURLBuild}/log/": 'pipeline-log.txt'])

  sh(label: 'Console log summary', script: 'tail -n 100 pipeline-log.txt > pipeline-log-summary.txt')

  def json = [:]
  json.job = readJSONOrDefault(file: "job-info.json")
  json.build = readJSONOrDefault(file: "build-info.json")
  json.test_summary = readJSONOrDefault(file: "tests-summary.json")
  json.test = readJSONOrDefault(file: "tests-info.json")
  json.changeSet = readJSONOrDefault(file: "changeSet-info.json")
  json.artifacts = readJSONOrDefault(file: "artifacts-info.json")
  json.steps = readJSONOrDefault(file: "steps-info.json")
  json.log = readFileOrDefault(file: "pipeline-log.txt")

  /** The build is not finished so we have to fix some values */
  json.build.result = currentBuild.currentResult
  json.build.state = 'FINISHED'
  json.build.durationInMillis = currentBuild.duration
  writeJSON(file: 'build-info.json' , json: toJSON(json.build), pretty: 2)

  writeJSON(file: 'build-report.json' , json: toJSON(json), pretty: 2)
}

def bulkDownload(map) {
  if(map.isEmpty()) {
    error('getBuildInfoJsonFiles: bulkDownload cannot be executed with empty arguments.')
  }
  def command = ['#!/usr/bin/env bash', 'set -x', 'source /usr/local/bin/bash_standard_lib.sh', 'status=0']
  map.each { url, file ->
    command << "(retry 3 curl -sfS --max-time 60 --connect-timeout 30 -o ${file} ${url}) || status=1"
    command << """[ -e "${file}" ] || echo "{}" > "${file}" """
  }
  command << 'exit ${status}'

  sh(label: 'Get Build info details', script: "${command.join('\n')}", returnStatus: true)
}

def readJSONOrDefault(map) {
  try {
    return readJSON(file: map.file)
  } catch(e) {
    return []
  }
}

def readFileOrDefault(map) {
  try {
    return readFile(file: map.file)
  } catch(e) {
    return []
  }
}
