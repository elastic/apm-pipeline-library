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

def call(Map args = [:]) {
  def credentialsId = args.get('credentialsId', env.JOB_GCS_CREDENTIALS)
  def bucket = args.containsKey('bucket') ? args.bucket : error('googleStorageUploadExt: bucket parameter is required')
  def pattern = args.containsKey('pattern') ? args.pattern : error('googleStorageUploadExt: pattern parameter is required')
  def sharedPublicly = args.get('sharedPublicly', false)

  def flags = ''

  if (sharedPublicly) {
    flags = '-a public-read'
  }

  if (anyMatchesGivenThePattern(pattern)) {
    return gsutil(command: "-m -q cp ${flags} ${pattern} ${bucket}", credentialsId: credentialsId)
  } else {
    log(level: 'WARN', text: "googleStorageUploadExt: There are no matches given the pattern '${pattern}'")
  }
}

def anyMatchesGivenThePattern(String pattern) {
  if (isUnix()) {
    return sh(returnStatus: true, script: "ls -1 ${pattern}") == 0
  } else {
    return powershell(returnStdout: true, script: "Get-ChildItem ${pattern} -recurse").contains("Mode")
  }
}
