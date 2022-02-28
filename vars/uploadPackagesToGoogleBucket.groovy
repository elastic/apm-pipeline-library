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
  Upload the given pattern files to the given bucket using an opinionated folder structure:
*/

def call(Map args = [:]) {
  def credentialsId = getArgumentOrFail(args, 'credentialsId', env.JOB_GCS_CREDENTIALS)
  def repoName = getArgumentOrFail(args, 'repo', env.REPO)
  def bucket = getArgumentOrFail(args, 'bucket', env.JOB_GCS_BUCKET)
  def pattern = args.get('pattern', "build/distributions/**/*")
  def folder = args.get('folder', '')

  def baseUri = "gs://${bucket}/${repoName}"
  def bucketUriCommit = "${baseUri}/commits/${env.GIT_BASE_COMMIT}"
  def bucketUriDefault = "${baseUri}/snapshots"

  if (isPR()) {
    bucketUriDefault = "${baseUri}/pull-requests/pr-${env.CHANGE_ID}"
  }

  [bucketUriDefault, bucketUriCommit].each { bucketUri ->
    def bucketUriWithFolder = folder?.trim() ? "${bucketUri}/${folder}" : bucketUri
    uploadPackages(bucketUri: "${bucketUriWithFolder}", pattern: pattern, credentialsId: credentialsId)
  }
}

def uploadPackages(Map args = [:]) {
  googleStorageUploadExt(
    bucket: args.bucketUri,
    credentialsId: args.credentialsId,
    pattern: args.pattern,
    sharedPublicly: true,
    extraFlags: '-r'
  )
}

def getArgumentOrFail(args, key, defaultValue) {
  if (args.containsKey(key)) {
    return args.get(key)
  } else {
    return (defaultValue?.trim()) ? defaultValue : error("uploadPackagesToGoogleBucket: ${key} parameter is required")
  }
}
