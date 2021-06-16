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
 This step helps to query what golang versions have been released.
*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('goVersion: windows is not supported yet.')
  }
  def action = args.containsKey('action') ? args.action : error('goVersion: action parameter is required.')
  def glob = args.get('glob', '')
  def unstable = args.get('unstable', false)

  switch(action) {
    case 'latest':
      def output = sh(label: "${action}", script: firstOne(getVersionsCommand(glob: glob, unstable: unstable)), returnStdout: true)
      return output.trim()
      break
    case 'versions':
      def output = sh(label: "${action}", script: getVersionsCommand(glob: glob, unstable: unstable), returnStdout: true)
      return output.trim()
      break
    default:
      error('goVersion: unsupported action.')
      break
  }
}

def removeGoPrefix(String command) {
  return command + ' | sed "s#^go##g"'
}

def firstOne(String command) {
  return command + ' | head -n1'
}

/**
* Query the tags with the form `go*`.
* Golang/go releases follow the format go<Major>.<Minor>(.<Patch>|rc[0-9]|beta[0-9])?
*/
def getAllGoVersions() {
  return 'git ls-remote --sort=-version:refname --tags --refs git://github.com/golang/go | sed "s#.*refs/tags/##g" | grep "go*"'
}

def getVersionsCommand(Map args = [:]) {
  def glob = args.get('glob', '')
  def unstable = args.get('unstable', false)
  def command = getAllGoVersions() + (unstable ? '' : ' | grep -v "[beta|rc]"')
  if (glob?.trim()) {
    command = command + ' | grep "' + glob + '"'
  }
  return removeGoPrefix(command)
}
