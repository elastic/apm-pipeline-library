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
  Untash the given stashed id, for such it downloads the given stashed id, and 
  uncompresses in the current location.

  The configuration can be delegated through env variables or explicitly. The 
  explicit parameters do have precedence over the environment variables.

  withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    unstashV2(name: 'source')
  }

  unstashV2(name: 'source', bucket: 'my-bucket', credentialsId: 'my-credentials')

  withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    // Even thought the env variable is set the bucket will 'foo' instead 'my-bucket'
    unstashV2(name: 'source', bucket: 'foo')
  }
*/
def call(Map params = [:]) {
  def name = params.containsKey('name') ? params.name : error('unstashV2: name param is required.')
  def bucket = params.containsKey('bucket') ? params.bucket : ''
  def credentialsId = params.containsKey('credentialsId') ? params.credentialsId : ''
  def filename = "${name}.tgz"

  if (bucket.trim()) {
    if (env.JOB_GCS_BUCKET) {
      log(level: 'WARNING', text: 'unstashV2: JOB_GCS_BUCKET is set. bucket param got precedency instead.')
    }
  } else {
    bucket = env.JOB_GCS_BUCKET ?: error('unstashV2: bucket param is required or JOB_GCS_BUCKET env variable.')
  }

  if (credentialsId.trim()) {
    if (env.JOB_GCS_CREDENTIALS) {
      log(level: 'WARNING', text: 'unstashV2: JOB_GCS_CREDENTIALS is set. credentialsId param got precedency instead.')
    }
  } else {
    credentialsId = env.JOB_GCS_CREDENTIALS ?: error('unstashV2: credentialsId param is required or JOB_GCS_CREDENTIALS env variable.')
  }

  if (!env.JOB_NAME || !env.BUILD_ID) {
    error('unstashV2: BUILD_ID and JOB_NAME environment variables are required.')
  }

  def bucketUri = "gs://${bucket}/${env.JOB_NAME}-${env.BUILD_ID}/${name}/${filename}"

  googleStorageDownload(
    bucketUri: "${bucketUri}",
    credentialsId: "${credentialsId}",
    localDirectory: '',
    pathPrefix: "${env.JOB_NAME}-${env.BUILD_ID}/${name}/"
  )

  extract(filename)
  cleanup(filename)
}

def extract(String filename) {  
  def command = "tar -xpf ${filename} ."
  if(isUnix()) {
    sh(label: 'Extract', script: command)
  } else {
    bat(label: 'Extract', script: command)
  }
}

def cleanup(String filename) {
  if(isUnix()) {
    sh(label: 'Cleanup', script: "rm ${filename}", returnStatus: true)
  } else {
    bat(label: 'Cleanup', script: "del ${filename}", returnStatus: true)
  }
}
