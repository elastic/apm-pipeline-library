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
  Given the bucket and google secrets then run the snapshoty to upload the artifacts to the
  google bucket
*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('snapshoty: windows is not supported yet.')
  }
  def bucketName = args.containsKey('bucket') ? args.bucket : error("snapshoty: bucket parameter is required")
  def gcsAccountSecret = args.containsKey('gcsAccountSecret') ? args.gcsAccountSecret : error("snapshoty: gcsAccountSecret parameter is required")
  def dockerRegistry = args.containsKey('dockerRegistry') ? args.dockerRegistry : error("snapshoty: dockerRegistry parameter is required")
  def dockerSecret = args.containsKey('dockerSecret') ? args.dockerSecret : error("snapshoty: dockerSecret parameter is required")

  dockerLogin(secret: dockerSecret, registry: dockerRegistry)

  withSecretVault(
    secret: gcsAccountSecret,
    data: [
      'client_email'  : 'GCS_CLIENT_EMAIL',
      'private_key'   : 'GCS_PRIVATE_KEY',
      'private_key_id': 'GCS_PRIVATE_KEY_ID',
      'project_id'    : 'GCS_PROJECT'
    ]
  ) {
    docker.image('docker.elastic.co/observability-ci/snapshoty:v1').inside() {
      sh(label: 'snapshoty upload', script: "snapshoty --config .ci/snapshoty.yml upload --bucket-name ${bucketName}")
    }
  }
}
