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
Configure the AWS context to run the given body closure

withAWSEnv(secret: 'foo') {
  // block
}
*/
def call(Map args = [:], Closure body) {
  if(!isUnix()){
    error('withAWSEnv: windows is not supported yet.')
  }
  def secret = args.containsKey('secret') ? args.secret : error('withAWSEnv: secret parameter is required.')
  def version = args.get('version', '2.4.2')
  def forceInstallation = args.get('forceInstallation', false)

  def awsUtilLocation = pwd(tmp: true)
  def secretFileLocation = "${awsUtilLocation}/aws-credentials.json"

  withEnv(["PATH+AWS=${awsUtilLocation}/aws-cli", "PATH+AWS_BIN=${awsUtilLocation}/bin"]) {
    if (forceInstallation || !isInstalled(tool: 'aws', flag: '--version', version: version)) {
      downloadAndInstall(awsUtilLocation, version)
    }
    def props = getVaultSecret(secret: secret)
    if (props?.errors) {
      error("withAWSEnv: Unable to get credentials from the vault: ${props.errors.toString()}")
    }
    def value = props?.data
    def credentialsContent = value?.csv
    if (!credentialsContent?.trim()) {
      error("withAWSEnv: Unable to read the credentials value")
    }
    writeFile(file: secretFileLocation, text: credentialsContent)
    // See https://awscli.amazonaws.com/v2/documentation/api/latest/reference/configure/import.html
    cmd(label: 'authenticate', script: 'aws configure import --csv ' + secretFileLocation)
    try {
      body()
    } finally {
      if (fileExists("${secretFileLocation}")) {
        if(isUnix()){
          sh "rm ${secretFileLocation}"
        } else {
          bat "del ${secretFileLocation}"
        }
      }
    }
  }
}

def downloadAndInstall(where, version) {
  def url = awsURL(version)
  def zipfile = 'awscli.zip'

  dir(where) {
    download(url: url, output: zipfile)
    unzip(quiet: true, zipFile: zipfile)
    sh(label: 'aws-install', script: "sh -x ./aws/install --install-dir ${where}/aws-cli --bin-dir ${where}/bin --update")
    sh(label: 'change permissions', script: "chmod 755 ./aws/dist/aws")
  }
}

def awsURL(version) {
  def url = 'https://awscli.amazonaws.com/awscli-exe'
  def arch = is64() ? 'x86_64' : 'x86'
  if (isUnix()) {
    return "${url}-linux-${arch}-${version}.zip"
  }
  error 'withAWSEnv: windows is not supported yet.'
}
