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

import groovy.transform.Field

@Field def ghLocation = ''

def call(Map args = [:]) {
  def command = args.containsKey('command') ? args.command : error('gh: command parameter is required.')
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def flags = args.get('flags', [:])
  def version = args.get('version', '1.9.2')
  def forceInstallation = args.get('forceInstallation', false)
  def forceRepo = args.get('forceRepo', false)

  // Use the current location as the git repo otherwise uses the env variables to pass
  // the repo information to the gh command or forceRepo to allow using the repo flag
  def isGitWorkspace = sh(label: 'isGitWorkspace', script: 'git rev-list HEAD -1 1> /dev/null 2>&1', returnStatus: true) == 0
  if (isGitWorkspace) {
    log(level: 'DEBUG', text: 'gh: running within a git workspace.')
  } else {
    if (forceRepo) {
      if (env.REPO_NAME?.trim() && env.ORG_NAME?.trim() && flags.containsKey('repo')) {
        log(level: 'WARN', text: 'gh: repo flag has higher precedence over REPO_NAME and ORG_NAME')
      }
    } else {
      log(level: 'DEBUG', text: 'gh: running outside of a git workspace. Using REPO_NAME and ORG_NAME if they are set')
      if (env.REPO_NAME?.trim() && env.ORG_NAME?.trim()) {
        flags['repo'] = "${env.ORG_NAME}/${env.REPO_NAME}"
      }
    }
  }

  if (ghLocation?.trim()) {
    log(level: 'DEBUG', text: 'gh: get the ghLocation from cache.')
  } else {
    log(level: 'DEBUG', text: 'gh: set the ghLocation.')
    ghLocation = pwd(tmp: true)
  }

  withEnv(["PATH+GH=${ghLocation}"]) {
    if (forceInstallation || !isInstalled(tool: 'gh', flag: '--version', version: version)) {
      if(isUnix()) {
        downloadInstaller(ghLocation, version)
      } else {
        installTools([[ tool: 'gh', version: version, provider: 'choco']])
      }
    }
    withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
      def flagsCommand = ''
      if (flags) {
        flags.each { k, v ->
          log(level: 'DEBUG', text: "gh: k ${k} - v ${v}")
          if (v instanceof java.util.ArrayList || v instanceof List) {
            v.findAll { it }.each { value ->
              flagsCommand += "--${k}='${normalise(value)}' "
            }
          } else {
            if (v) {
              flagsCommand += "--${k}='${normalise(v)}' "
            }
          }
        }
      }
      return runCommand(command, flagsCommand)
    }
  }
}

def runCommand(command, flagsCommand) {
  def output = cmd(label: "gh ${command}", script: "gh ${command} ${flagsCommand}", returnStdout: true)
  return output
}

def downloadInstaller(where, version) {
  def os = nodeOS()
  if (os.equals('darwin')) {
    os = 'macOS'
  }
  def arch = (isArm()) ? 'arm64' : 'amd64'
  def url = "https://github.com/cli/cli/releases/download/v${version}/gh_${version}_${os}_${arch}.tar.gz"
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

/**
* Ensure ' is replaced to avoid any issues when using the markdown templating.
*/
def normalise(v) {
  if (v instanceof String) {
    return v?.toString()?.replaceAll("'",'"')
  }
  return v
}
