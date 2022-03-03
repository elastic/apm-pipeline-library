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
Configure the GCP context to run the given body closure

withGCPEnvContext(credentialsId: 'foo') {
  // block
}
*/
def call(Map args = [:], Closure body) {
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : ''
  def secret = args.containsKey('secret') ? args.secret : ''
  def secretFileLocation = args.containsKey('secretFileLocation') ? args.secretFileLocation : pwd(tmp: true) + "/google-cloud-credentials.json"

  if (!credentialsId?.trim() && !secret?.trim()) {
    error('withGCPEnvContext: credentialsId or secret parameters are required.')
  }

  if (secret) {
    def props = getVaultSecret(args)
    if (props?.errors) {
      error "withGCPEnv: Unable to get credentials from the vault: ${props.errors.toString()}"
    }
    def value = props?.data
    def credentialsContent = readCredentialsContent(value)
    writeFile(file: secretFileLocation, text: credentialsContent)
  }
  try {
    if (secret) {
      // Somehow the login works for google bucket integrations but something
      // it's not right when using the VM creation with terraform and GCP
      // Setting GOOGLE_APPLICATION_CREDENTIALS seems to be the workaround
      // https://cloud.google.com/docs/authentication/getting-started
      withEnv(["GOOGLE_APPLICATION_CREDENTIALS=${secretFileLocation}"]){
        body()
      }
    } else {
      withCredentials([file(credentialsId: credentialsId, variable: 'FILE_CREDENTIAL')]) {
        body()
      }
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

/**
* Read the vault secret and look for the required fields.
* * credentials field is the one initially supported and kept for backward compatibility reasons.
* * value field is the fallback field, this is the case used
*/
def readCredentialsContent(Map vaultSecretContent) {
  def credentialsContent = vaultSecretContent?.credentials
  if (credentialsContent?.trim()) {
    log(level: 'INFO', text: "readCredentialsContent: reading the 'credentials' field.")
    return credentialsContent
  }
  credentialsContent = vaultSecretContent?.value
  if (credentialsContent?.trim()) {
    log(level: 'INFO', text: "readCredentialsContent: reading the 'value' field.")
    return credentialsContent
  }
  error "withGCPEnv: Unable to read the credentials and value fields"
}
