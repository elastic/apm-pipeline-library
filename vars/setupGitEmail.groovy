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
 Configure the git email for the current workspace or globally.
*/
def call(Map params = [:]) {
  if(!isUnix()){
    error('setupGitEmail: windows is not supported yet.')
  }
  def flag = params.containsKey('global') ? (params.global ? '--global' : '') : ''
  sh(label: 'Git config', script: """
    git config ${flag} user.name apmmachine
    git config ${flag} user.email 58790750+apmmachine@users.noreply.github.com
  """)
}
