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
  Publish to the CDN the given set of source files to the target bucket
  with the given headers.

  // This command would upload all js files files in the packages/rum/dist/bundles directory
  // and make them readable and cacheable, with cache expiration of one hour and a custom
  // metadata.
  publishToCDN(headers: ["Cache-Control:public,max-age=3600", "x-goog-meta-reviewer:v1v"],
               source: 'packages/rum/dist/bundles/*.js',
               target: "gs://beats-ci-temp/rum/5.1.0",
               secret: 'secret/observability-team/ci/service-account/test-google-storage-plugin')
*/
def call(Map params = [:]){
  if(!isUnix()){
    error('publishToCDN: windows is not supported yet.')
  }
  def install = params.get('install', true)
  def forceInstall = params.get('forceInstall', true)
  def headers = params.containsKey('headers') ? params.headers.toList() : []
  def source = params.containsKey('source') ? params.source : error('publishToCDN: Missing source argument.')
  def target = params.containsKey('target') ? params.target : error('publishToCDN: Missing target argument.')
  def secret = params.containsKey('secret') ? params.secret : error('publishToCDN: Missing secret argument.')
  def keyFile = 'service-account.json'

  def installPath = "${env.HOME}/google-cloud-sdk"

  if(install) {
    if(forceInstall) {
      sh(label: 'Delete install folder', script: "rm -rf ${installPath}")
    }
    withEnv(["CLOUDSDK_CORE_DISABLE_PROMPTS=1"]){
      sh(label: 'Install gcloud', script: 'curl -s https://sdk.cloud.google.com | bash > install.log')
    }
  }
  prepareCredentials(keyFile: keyFile, secret: secret)
  if(install) {
    // path argument is a workaround since withEnv(PATH) does not work within the docker.inside step
    upload(keyFile: keyFile, source: source, target: target, headers: headers, path: "${installPath}/bin")
  } else {
    upload(keyFile: keyFile, source: source, target: target, headers: headers)
  }
}

def prepareCredentials(Map params = [:]) {
  def props = getVaultSecret(secret: params.secret)
  if (props?.errors) {
    error "publishToCDN: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  writeJSON file: params.keyFile, json: props.data.value
}

def upload(Map params = [:]) {
  def pathPrepend = (params.containsKey('path') ? "PATH=${params.path}:\${PATH}" : '')
  try {
    sh(label: 'Activate service account', script: "${pathPrepend} gcloud auth activate-service-account --key-file=${params.keyFile}")
    def headersFlag = (params.headers.isEmpty() ? '' : params.headers.collect{ "-h ${it}" }.join(' '))
    sh(label: 'Upload', script: "${pathPrepend} gsutil ${headersFlag} cp ${params.source} ${params.target}")
  } catch (err) {
    error "publishToCDN: error ${err}"
    throw err
  } finally {
    sh(label: 'Rollback context', script: "rm ${params.keyFile}")
  }
}
