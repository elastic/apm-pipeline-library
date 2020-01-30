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
  Wrap the npmrc token
  withNpmrc(path: '/foo', npmrcFile: '.npmrc') {
    // block
  }
*/

def call(Map params = [:], Closure body) {
  def path = params.containsKey('path') ? params.path : env.HOME
  def registry = params.containsKey('registry') ? params.registry : 'registry.npmjs.org'
  def npmrcFile = params.containsKey('npmrcFile') ? params.npmrcFile : '.npmrc'
  def secret = params.containsKey('secret') ? params.secret : 'secret/apm-team/ci/elastic-observability-npmjs'

  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withNpmrc: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def token = props?.data?.token
  if (token == null) {
    error 'withNpmrc: was not possible to get authentication info'
  }
  dir(path) {
    writeFile file: npmrcFile, text: "//${registry}/:_authToken=${token}"
  }
  try {
    body()
  } catch (err) {
    error "withNpmrc: error ${err}"
    throw err
  } finally {
    // ensure any sensitive details are deleted
    dir(path) {
      if (fileExists("${npmrcFile}")) {
        if(isUnix()){
          sh "rm ${npmrcFile}"
        } else {
          bat "del ${npmrcFile}"
        }
      }
    }
  }
}
