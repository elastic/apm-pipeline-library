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
Configure the Terraform context to run the given body closure

withTerraformEnv(version: '0.15.1') {
  // block
}
*/
def call(Map args = [:], Closure body) {
  def version = args.get('version', '1.1.9')
  def forceInstallation = args.get('forceInstallation', false)
  def noCheckCertificate = args.get('noCheckCertificate', false)

  def location = pwd(tmp: true)

  withEnv(["PATH+TERRAFORM=${location}"]) {
    if (forceInstallation || !isInstalled(tool: 'terraform', flag: '--version', version: version)) {
      downloadAndInstall(location, version, noCheckCertificate)
    }
    body()
  }
}

def downloadAndInstall(where, version, noCheckCertificate) {
  def url = terraformURL(version)
  def zipfile = 'terraform.zip'
  def wgetFlags = noCheckCertificate ? '--no-check-certificate' : ''
  dir(where) {
    retryWithSleep(retries: 5, seconds: 10, backoff: true) {
      download(url: url, output: zipfile, wgetFlags: wgetFlags)
    }
    unzip(quiet: true, zipFile: zipfile)
    if (isUnix()) {
      sh(label: 'chmod terraform', script: 'chmod +x terraform')
    }
  }
}

def terraformURL(version) {
  def url = 'https://releases.hashicorp.com/terraform'
  def arch = is64() ? 'amd64' : '386'
  if (isArm()) {
    arch = is64() ? 'arm64' : 'arm'
  }
  return "${url}/${version}/terraform_${version}_${nodeOS()}_${arch}.zip"
}
