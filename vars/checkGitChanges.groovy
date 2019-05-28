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
  use git diff to check the changes on a path, then return true or false.

  def numOfChanges = checkGitChanges(target: env.CHANGE_TARGET,commit: env.GIT_SHA,prefix: '_beats')
*/
def call() {
  def target =  params.containsKey('target') ? params.target : error("checkGitChanges: not valid target")
  def commit =  params.containsKey('commit') ? params.commit : error("checkGitChanges: not valid commit")
  def regexps =  params.containsKey('regexps') ? params.prefix : error("checkGitChanges: not valid prefix")

  def changes = sh(script: "git diff --name-only ${target}...${commit} > git-diff.txt",returnStdout: true)
  def match = regexps.find{ regexp ->
      sh(script: "grep '${regexp}' git-diff.txt",returnStatus: true) == 0
  }
  return (match != null)
}
