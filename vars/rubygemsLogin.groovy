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
  Login to Rubygems.com with an authentication credentials from a Vault secret.
  The vault secret contains `user` and `password` fields with the authentication details.

  rubygemsLogin(secret: 'secret/team/ci/secret-name') {
    sh 'gem push x.y.z'
  }

  rubygemsLogin.withApi(secret: 'secret/team/ci/secret-name') {
    sh 'gem push x.y.z'
  }
*/
def call(Map args = [:], Closure body) {
  if(!isUnix()){
    error('rubygemsLogin: windows is not supported yet.')
  }
  def secret = args.containsKey('secret') ? args.secret : error('rubygemsLogin: No valid secret to looking for.')

  def props = getVaultSecret(secret: secret)

  if(props?.errors){
     error "rubygemsLogin: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def data = props?.data
  def rubyUser = data?.user
  def rubyPass = data?.password

  if(data == null || rubyUser == null || rubyPass == null){
    error 'rubygemsLogin: was not possible to get authentication details.'
  }

  def rubyUserVariable = 'RUBY_USER'
  def rubyPassVariable = 'RUBY_PASS'

  wrap([$class: 'MaskPasswordsBuildWrapper',
        varPasswordPairs: [[var: rubyUserVariable, password: rubyUser],
                           [var: rubyPassVariable, password: rubyPass]]]) {
    withEnv(["${rubyUserVariable}=${rubyUser}", "${rubyPassVariable}=${rubyPass}"]) {
      sh(label: "rubygems login", script: """#!/bin/bash
      set +x
      curl -u "\${${rubyUserVariable}}:\${${rubyPassVariable}}" https://rubygems.org/api/v1/api_key.yaml > ~/.gem/credentials
      chmod 0600 ~/.gem/credentials
      """)
    }
  }
  try {
    body()
  } catch (err) {
    throw err
  } finally {
    sh '[ -e ~/.gem/credentials ] && rm ~/.gem/credentials || true'
  }
}

def withApi(Map args = [:], Closure body) {
  if(!isUnix()){
    error('rubygemsLogin.withApi: windows is not supported yet.')
  }
  def secret = args.containsKey('secret') ? args.secret : error('rubygemsLogin.withApi: No valid secret to looking for.')

  def props = getVaultSecret(secret: secret)

  if(props?.errors){
     error "rubygemsLogin.withApi: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def data = props?.data
  def rubyApiKey = data?.apiKey

  if(data == null || rubyApiKey == null){
    error 'rubygemsLogin.withApi: was not possible to get authentication details.'
  }

  def rubyApiKeyVariable = 'RUBY_API_KEY'

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[var: rubyApiKeyVariable, password: rubyApiKey]]]) {
    withEnv(["${rubyApiKeyVariable}=${rubyApiKey}"]) {
      sh(label: "rubygems login", script: """#!/bin/bash
      set +x
      mkdir ~/.gem
      echo '---' > ~/.gem/credentials
      echo ":rubygems_api_key: \${${rubyApiKeyVariable}}" >> ~/.gem/credentials
      chmod 0600 ~/.gem/credentials""")
    }
  }
  try {
    body()
  } catch (err) {
    throw err
  } finally {
    sh '[ -e ~/.gem/credentials ] && rm ~/.gem/credentials || true'
  }
}
