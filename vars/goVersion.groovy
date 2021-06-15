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
  def glob = args.get('glob', null)

  switch(action) {
    case 'latest-version':
      cloneAndRun() {
        def output = sh(label: "${action}", script: firstOne(getAllGoVersions()), returnStdout: true)
        return output.trim()
      }
      break
    case 'latest-versions':
      cloneAndRun() {
        def output = sh(label: "${action}", script: getAllGoVersions(), returnStdout: true)
        return output.trim()
      }
      break
    case 'latest-release-version':
      cloneAndRun() {
        def output = sh(label: "${action}", script: firstOne(getLatestVersionCommand(glob)), returnStdout: true)
        return output.trim()
      }
      break
    default:
      error('goVersion: unsupported action.')
      break
  }
}

def firstOne(String command) {
  return command + ' | head -n1'
}

/**
* Query the tags with the form `go*`.
* Golang/go releases follow the format goMajor.Minor.Patch<rc|beta
*/
def getAllGoVersions() {
  return 'git tag --list --sort=-version:refname "go*"'
}

def getLatestVersionCommand(String glob = null) {
  def command = getAllGoVersions + ' | grep -v "[beta|rc]"'
  if (glob) {
    command = command + ' | grep "' + glob + '"'
  }
  command = command + ' | head -n'
  return command
}

def cloneAndRun(Closure body) {
  def workspaceLocation = pwd(tmp: true)
  dir(workspaceLocation) {
    sh(label: 'git clone golang/go',
       script: '''
        git clone git@github.com:golang/go
        git fetch --tags
       ''')
    body()
  }
}
