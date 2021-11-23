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
Configure the Gh context to run the given body closure

withGhEnv(credentialsId: 'foo') {
  // block
}
*/

import groovy.transform.Field

@Field def ghLocation = ''

def call(Map args = [:], Closure body) {
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def version = args.get('version', '1.9.2')
  def forceInstallation = args.get('forceInstallation', false)

  if (ghLocation?.trim()) {
    log(level: 'DEBUG', text: 'withGhEnv: get the ghLocation from cache.')
  } else {
    log(level: 'DEBUG', text: 'withGhEnv: set the ghLocation.')
    ghLocation = pwd(tmp: true)
  }

  withEnv(["PATH+GH=${ghLocation}"]) {
    if (forceInstallation || !isInstalled(tool: 'gh', flag: '--version', version: version)) {
      downloadInstaller(ghLocation, version)
    }
    withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
      body()
    }
  }
}

def downloadInstaller(where, version) {
  def os = nodeOS()
  if (os.equals('darwin')) {
    os = 'macOS'
  }
  def arch = (isArm()) ? 'arm64' : 'amd64'
  def url = "https://github.com/cli/cli/releases/download/v${version}/gh_${version}_${os}_${arch}.tar.gz"
  def tarball = 'gh.tar.gz'

  if(isUnix()) {
    dir(where) {
      if (!downloadWithWget(tarball, url)) {
        downloadWithCurl(tarball, url)
      }
      uncompress(tarball)
    }
  } else {
    installTools([[ tool: 'gh', version: version, provider: 'choco']])
  }
}

def downloadWithWget(tarball, url) {
  if(isInstalled(tool: 'wget', flag: '--version')) {
    retryWithSleep(retries: 3, seconds: 5, backoff: true) {
      sh(label: 'download gh', script: "wget -q -O ${tarball} ${url}")
    }
    return true
  } else {
    log(level: 'WARN', text: 'withGhEnv: wget is not available. gh will not be installed then.')
  }
  return false
}

def downloadWithCurl(tarball, url) {
  if(isInstalled(tool: 'curl', flag: '--version')) {
    sh(label: 'download gsutil', script: "curl -sSLo ${tarball} --retry 3 --retry-delay 2 --max-time 10 ${url}")
  } else {
    log(level: 'WARN', text: 'withGhEnv: curl is not available. gh will not be installed then.')
  }
}

def uncompress(tarball) {
  sh(label: 'untar gh', script: "tar -xpf ${tarball} --strip-components=2")
}
