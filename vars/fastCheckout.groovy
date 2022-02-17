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

  Builds the Docker image for Kibana, from a branch or a pull Request.

  fastCheckout(refspec: 'main', depth: 10)
  fastCheckout(refspec: 'PR/12345')
  fastCheckout(refspec: 'aa3bed18072672e89a8e72aec43c96831ff2ce05')
*/

def call(Map args = [:]){
  def baseDir = args.baseDir ?: "."
  def credentialsId = args.credentialsId ?: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'
  def referenceRepo = args.reference ?: ""
  def depth = args.depth as Integer ?: 1
  def shallow = args.shallow ?: true
  def url = args.url ?: error("The Git URL is required.")
  def refspec = args?.refspec?.trim() ?: 'main'

  if(isCommitSha1("${refspec}")){
    efectiveBranch = "${refspec}"
    efectiveRefspec = "${refspec}"
  } else if("${refspec}".startsWith('PR/')){
    def prID = "${refspec}".split('/')[1]
    efectiveBranch = "origin/PR/${prID}"
    efectiveRefspec = "+refs/pull/${prID}/head:refs/remotes/origin/PR/${prID}"
  } else {
    efectiveBranch =  "origin/${refspec}"
    efectiveRefspec = "+refs/heads/${refspec}:refs/remotes/origin/${refspec}"
  }

  checkout([$class: 'GitSCM',
    branches: [[name: "${efectiveBranch}"]],
    extensions: [
      [$class: 'RelativeTargetDirectory', relativeTargetDir: "${baseDir}"],
      [$class: 'CheckoutOption', timeout: 15],
      [$class: 'AuthorInChangelog'],
      [$class: 'IgnoreNotifyCommit'],
      [$class: 'CloneOption',
      depth: depth,
      honorRefspec: true,
      noTags: true,
      reference: "${referenceRepo}",
      shallow: true,
      timeout: 15
    ]],
    userRemoteConfigs: [[
      name: 'origin',
      credentialsId: "${credentialsId}",
      refspec: "${efectiveRefspec}",
      url: "${url}"
    ]]
  ])
}

def isCommitSha1(branch){
  return branch.matches('^[a-fA-F0-9]{40}$')
}
