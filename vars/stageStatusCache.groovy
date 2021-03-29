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
def stageStatusCache(Map args, Closure body){
  if(!args.containsKey('bucket')){
    args['bucket'] = 'beats-ci-temp'
  }
  if(!args.containsKey('credentialsId')){
    args['credentialsId'] = 'beats-ci-gcs-plugin-file-credentials'
  }
  if(!args.containsKey('sha')){
    args['sha'] = "${GIT_BASE_COMMIT}"
  }
  if(readStageStatus(args) == false || isUserTrigger() || env.BUILD_ID == "1" || env.RUN_ALL == "1"){
    body()
    saveStageStatus(args)
  } else {
    log(level: 'INFO', text: "The stage skiped because it is in the execution cache.")
  }
}

/**
  Save the status file of the stage.
*/
def saveStageStatus(Map args){
  def statusFileName = stageStatusId(args)
  writeFile(file: statusFileName, text: "OK")
  googleStorageUploadExt(bucket: "gs://${args.bucket}/ci/cache/",
    credentialsId: "${args.credentialsId}",
    pattern: "${statusFileName}",
    sharedPublicly: true)
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
  } catch(e) {
    log(level: 'WARN', text: "There is no cache file for the current stage.")
  } finally {
    return fileExists("${statusFileName}")
  }
}

/**
  generate an unique ID for the stage and commit.
*/
def stageStatusId(Map args){
  return base64encode(text: "${args.id}${args.sha}", encoding: "UTF-8")
}
