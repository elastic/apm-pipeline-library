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
Configure Packer context to run the given body closure

withPackerEnv(version: '0.15.1') {
  // block
}
*/
def call(Map args = [:], Closure body) {
  def version = args.get('version', '1.8.4')
  def forceInstallation = args.get('forceInstallation', false)

  def location = pwd(tmp: true)

  withEnv(["PATH+PACKER=${location}"]) {
    if (forceInstallation || !isInstalled(tool: 'packer', flag: '--version', version: version)) {
      downloadAndInstall(location, version)
    }
    body()
  }
}

def downloadAndInstall(where, version) {
  def url = packerURL(version)
  def zipfile = 'packer.zip'
  dir(where) {
    retryWithSleep(retries: 5, seconds: 10, backoff: true) {
      download(url: url, output: zipfile)
    }
    unzip(quiet: true, zipFile: zipfile)
    if (isUnix()) {
      sh(label: 'chmod packer', script: 'chmod +x packer')
    }
  }
}

def packerURL(version) {
  def url = 'https://releases.hashicorp.com/packer'
  def arch = is64() ? 'amd64' : '386'
  if (isArm()) {
    arch = is64() ? 'arm64' : 'arm'
  }
  return "${url}/${version}/packer_${version}_${nodeOS()}_${arch}.zip"
}
