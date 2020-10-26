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
  Wrapper to interact with the gh command line. It returns the stdout output.

  // List all the open issues with the label 
  gh(command: 'issue list', flags: [ label: ['flaky-test'], state: 'open' ])

  // Create issue with title and body
  gh(command: 'issue create', flags: [ title: "I found a bug", body: "Nothing works" ])

*/

def call(Map args = [:]) {
  if(!isUnix()) {
    error 'gh: windows is not supported yet.'
  }
  def command = args.containsKey('command') ? args.command : error('gh: command argument is required.')
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def flags = args.get('flags', [])
  withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
    def flagsCommand = ''
    if (flags) {
      flags.each { k, v ->
        if (v instanceof java.util.ArrayList) {
          v.each { value ->
            flagsCommand += "--${k}=${value} "
          }
        } else {
          flagsCommand += "--${k}=${v} "
        }
      }
    }

    if(isInstalled(tool: 'gh', flag: '--version')) {
      return runCommand(command, flagsCommand)
    } else {
      def ghLocation = pwd(tmp: true)
      downloadInstaller(ghLocation)
      withEnv(["PATH+GH=${ghLocation}"]) {
        return runCommand(command, flagsCommand)
      }
    }
  }
}

def runCommand(command, flagsCommand) {
  def output = sh(label: "gh ${command}", script: "gh ${command} ${flagsCommand}", returnStdout: true)
  return output
}

def downloadInstaller(where) {
  def url = 'https://github.com/cli/cli/releases/download/v1.1.0/gh_1.1.0_linux_amd64.tar.gz'
  def tarball = 'gh.tar.gz'
  if(isInstalled(tool: 'wget', flag: '--version')) {
    dir(where) {
      retryWithSleep(retries: 3, seconds: 5, backoff: true) {
        sh(label: 'download gh', script: "wget -q -O ${tarball} ${url}")
        sh(label: 'untar gh', script: "tar -xpf ${tarball} --strip-components=2")
      }
    }
  } else {
    log(level: 'WARN', text: 'gh: wget is not available. gh will not be installed then.')
  }
}
