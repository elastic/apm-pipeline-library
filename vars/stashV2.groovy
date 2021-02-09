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
  Stash the current location, for such it compresses the current path and
  upload it to Google Storage.

  The configuration can be delegated through env variables or explicitly. The 
  explicit parameters do have precedence over the environment variables.

  withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    stashV2(name: 'source')
  }

  stashV2(name: 'source', bucket: 'my-bucket', credentialsId: 'my-credentials')

  withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    // Even thought the env variable is set the bucket will 'foo' instead 'my-bucket'
    stashV2(name: 'source', bucket: 'foo')
  }
*/
def call(Map args = [:]) {
  def name = args.containsKey('name') ? args.name : error('stashV2: name parameter is required.')
  def bucket = args.containsKey('bucket') ? args.bucket : ''
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : ''
  def filename = "${name}.tgz"

  if (bucket.trim()) {
    if (env.JOB_GCS_BUCKET) {
      log(level: 'WARNING', text: 'stashV2: JOB_GCS_BUCKET is set. bucket param got precedency instead.')
    }
  } else {
    bucket = env.JOB_GCS_BUCKET ?: error('stashV2: bucket parameter is required or JOB_GCS_BUCKET env variable.')
  }

  if (credentialsId.trim()) {
    if (env.JOB_GCS_CREDENTIALS) {
      log(level: "WARNING", text: 'stashV2: JOB_GCS_CREDENTIALS is set. credentialsId param got precedency instead.')
    }
  } else {
    credentialsId = env.JOB_GCS_CREDENTIALS ?: error('stashV2: credentialsId parameter is required or JOB_GCS_CREDENTIALS env variable.')
  }

  if (!env.JOB_NAME || !env.BUILD_ID) {
    error('stashV2: BUILD_ID and JOB_NAME environment variables are required.')
  }

  compress(filename)

  def bucketUri = "gs://${bucket}/${env.JOB_NAME}-${env.BUILD_ID}/${name}"
  googleStorageUpload(
    bucket: "${bucketUri}",
    credentialsId: "${credentialsId}",
    pattern: "${filename}",
    sharedPublicly: false,
    showInline: true
  )

  cleanup(filename)

  return "${bucketUri}/${filename}"
}

def compress(String filename) {
  tar(file: filename, dir: '.', archive: false, failNever: false)
}

def cleanup(String filename) {
  if(isUnix()) {
    sh(label: 'Cleanup', script: "rm ${filename}", returnStatus: true)
  } else {
    bat(label: 'Cleanup', script: "del ${filename}", returnStatus: true)
  }
}
