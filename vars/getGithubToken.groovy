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
  return the Github token.
*/
def call(Map args = [:]){
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : "2a9602aa-ab9f-4e52-baf3-b71ca88469c7"
  def githubToken
  withCredentials([[
    variable: "GITHUB_TOKEN",
    credentialsId: credentialsId,
    $class: "StringBinding",
  ]]) {
    githubToken = env.GITHUB_TOKEN
  }
  return githubToken
}
