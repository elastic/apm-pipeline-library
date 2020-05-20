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

  publishToCDN(job: 'foo', parameters: [string(name: "my param", value: some_value)])
*/
def call(Map params = [:]){
  if(!isUnix()){
    error('publishToCDN: windows is not supported yet.')
  }
  def install = params.get('install', true)
  def header = params.get('header', '')
  def source = params.containsKey('source') ? params.source : error('publishToCDN: Missing source argument.')
  def target = params.containsKey('target') ? params.target : error('publishToCDN: Missing target argument.')
  def projectId = params.containsKey('projectId') ? params.projectId : error('publishToCDN: Missing projectId argument.')
  def secret = params.containsKey('secret') ? params.secret : error('publishToCDN: Missing secret argument.')

  if(install) {
    // TODO install google cloud tooling
    // see https://cloud.google.com/storage/docs/gsutil_install
  }

  // Prepare the credentials
  def keyFile = 'service-account.json'
  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "publishToCDN: Unable to get credentials from the vault: ${props.errors.toString()}"
  }

  // Upload with the required headers.
  try {
    writeJSON file: keyFile, json: props.data.value
    sh(label: 'Activate service account', script: "gcloud auth activate-service-account --key-file=${keyFile} --project=${projectId}")
    def headerFlag = (header.trim() ? "-h ${header}" : '')
    sh(label: 'Upload', script: "gsutil ${headerFlag} cp -r ${source} ${target}")
  } catch (err) {
    error "publishToCDN: error ${err}"
    throw err
  } finally {
    // Rollback to the previous release context
    sh(label: 'Rollback context', script: "rm ${keyFile}")
  }
}
