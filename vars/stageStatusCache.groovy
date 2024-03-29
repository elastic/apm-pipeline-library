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
  Execute the body if there is not stage status file for the stage and the commit we are building.
  User triggered builds will execute all stages always.
  If the stage success the status is save in a file.
*/
def call(Map args, Closure body){
  if(!args.containsKey('bucket')){
    args['bucket'] = 'beats-ci-temp'
  }
  if(!args.containsKey('credentialsId')){
    args['credentialsId'] = 'beats-ci-gcs-plugin-file-credentials'
  }
  if(!args.containsKey('sha')){
    args['sha'] = "${env.GIT_BASE_COMMIT}"
  }
  if(isUserTrigger() || args.get('runAlways', false) || readStageStatus(args) == false){
    log(level: 'INFO', text: "The stage '${args.id}' cannot be cached.")
    body()
    saveStageStatus(args)
  } else {
    log(level: 'INFO', text: "The stage '${args.id}' is skipped because it is in the execution cache.")
  }
}

/**
  Save the status file of the stage.
*/
def saveStageStatus(Map args){
  def statusFileName = stageStatusId(args)
  writeFile(file: statusFileName, text: "OK")
  // Retry in case the google storage is temporarily not accessible.
  retryWithSleep(retries: 3, seconds: 5, backoff: true) {
    googleStorageUploadExt(bucket: "gs://${args.bucket}/ci/cache/",
      credentialsId: "${args.credentialsId}",
      pattern: "${statusFileName}",
      sharedPublicly: false)
  }
}

/**
  Read the status file of the stage if it exists.
*/
def readStageStatus(Map args){
  def statusFileName = stageStatusId(args)
  try {
    cmd(label: 'Download Stage Status',
      script: "curl -sSf -O https://storage.googleapis.com/${args.bucket}/ci/cache/${statusFileName}",
      returnStatus: true)
  } finally {
    return fileExists("${statusFileName}")
  }
}

/**
  generate an unique ID for the stage and commit.
*/
def stageStatusId(Map args){
  return base64encode(text: "${args.id}${args.sha}", encoding: "UTF-8", padding: false)
}
