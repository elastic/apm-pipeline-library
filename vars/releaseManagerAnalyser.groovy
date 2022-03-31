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
Given the release manager output then it analyses the failure if any, and returns
the digested output to the end user.
*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('releaseManagerAnalyser: windows is not supported yet.')
  }
  def ret = ''
  def output = args.containsKey('file') ? args.file : error('releaseManagerAnalyser: file parameter is required.')
  def reportFile = "${pwd(tmp: true)}/release-manager-report.out"
  withEnv(["RAW_OUTPUT=${output}", "REPORT=${reportFile}"]) {
    sh(label: 'Release Manager analyser', script: libraryResource('scripts/release-manager-analyser.sh'))
    if (fileExists("${reportFile}")) {
      ret = readFile(file: reportFile)
    }
  }
  return ret
}
