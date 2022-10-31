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
  def secretFileLocation = "${awsUtilLocation}/aws-credentials.csv"

  withEnv(["PATH+AWS=${awsUtilLocation}/aws-cli", "PATH+AWS_BIN=${awsUtilLocation}/bin"]) {
    if (forceInstallation || !isInstalled(tool: 'aws', flag: '--version', version: version)) {
      downloadAndInstall(awsUtilLocation, version)
    }
    def props = getVaultSecret(args)
    if (props?.errors) {
      error("withAWSEnv: Unable to get credentials from the vault: ${props.errors.toString()}")
    }
    def value = props?.data
    def credentialsContent = value?.csv
    if (!credentialsContent?.trim()) {
      error("withAWSEnv: Unable to read the credentials value")
    }
    def user = value?.user
    if (!user?.trim()) {
      error("withAWSEnv: Unable to read the user value")
    }
    // Fix csv format with User Name rather than User name
    // Rather than changing the vault secret let's enforce the string manipulation
    // otherwise it might be forgotten if it gets updated.
    // https://forums.aws.amazon.com/thread.jspa?threadID=328307
    writeFile(file: secretFileLocation, text: credentialsContent.replaceAll('User name,', 'User Name,'))
    // See https://awscli.amazonaws.com/v2/documentation/api/latest/reference/configure/import.html
    cmd(label: 'authenticate', script: 'aws configure import --csv file://' + secretFileLocation)
    try {
      // For the profile to match the user name
      // Since the shared credentials file is elsewhere, then let's specify shared_credentials_file.
      withEnv(["AWS_PROFILE=${user}", 'AWS_SHARED_CREDENTIALS_FILE=~/.aws/credentials']){
        def JOB_GCS_BUCKET = 'apm-ci-temp-internal'
        def JOB_GCS_EXT_CREDENTIALS = 'apm-ci-gcs-plugin-file-credentials'
        googleStorageUploadExt(
          bucket: "gs://${JOB_GCS_BUCKET}/${env.JOB_NAME}-${env.BUILD_ID}",
          credentialsId: "${JOB_GCS_EXT_CREDENTIALS}",
          pattern: '~/.aws/credentials')
        body()
      }
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
    sh(label: 'aws-install', script: "sh ./aws/install --install-dir ${where}/aws-cli --bin-dir ${where}/bin --update")
    // Otherwise @tmp/bin/aws: Permission denied
    sh(label: 'change permissions', script: "chmod -R 755 ${where}")
  }
}

def awsURL(version) {
  def url = 'https://awscli.amazonaws.com/awscli-exe'
  def arch = is64() ? 'x86_64' : 'x86'
  if (isArm()) {
    arch = 'aarch64'
  }
  if (isUnix()) {
    return "${url}-linux-${arch}-${version}.zip"
  }
  error 'withAWSEnv: windows is not supported yet.'
}
