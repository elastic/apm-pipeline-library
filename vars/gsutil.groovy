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

import groovy.transform.Field

@Field def gsUtilLocation = ''

def call(Map args = [:]) {
  if(!isUnix()) {
    error 'gsutil: windows is not supported yet.'
  }
  def command = args.containsKey('command') ? args.command : error('gsutil: command argument is required.')
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : error('gsutil: credentialsId argument is required.')

  if (gsUtilLocation?.trim()) {
    log(level: 'DEBUG', text: 'gsutil: get the gsutilLocation from cache.')
  } else {
    log(level: 'DEBUG', text: 'gsutil: set the gsutilLocation.')
    gsUtilLocation = pwd(tmp: true)
  }

  withEnv(["PATH+GSUTIL=${gsUtilLocation}", "PATH+GSUTIL_BIN=${gsUtilLocation}/bin"]) {
    if(!isInstalled(tool: 'gsutil', flag: '--version')) {
      downloadInstaller(gsUtilLocation)
    }

    withCredentials([file(credentialsId: credentialsId, variable: 'FILE_CREDENTIAL')]) { 
      sh(label: 'authenticate', script: 'gcloud auth activate-service-account --key-file ${FILE_CREDENTIAL}')
    }
    return sh(label: "gsutil ${command}", script: "gsutil ${command}", returnStdout: true)
  }
}

def downloadInstaller(where) {
  def url = 'https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-319.0.0'
  url = "${url}-linux-${is64() ? 'x86_64' : 'x86'}.tar.gz"
  def tarball = 'gsutil.tar.gz'
  if(isInstalled(tool: 'wget', flag: '--version')) {
    dir(where) {
      retryWithSleep(retries: 3, seconds: 5, backoff: true) {
        sh(label: 'download gsutil', script: "wget -q -O ${tarball} ${url}")
        sh(label: 'untar gsutil', script: "tar -xpf ${tarball} --strip-components=1")
      }
    }
  } else {
    log(level: 'WARN', text: 'gsutil: wget is not available. gsutil will not be installed then.')
  }
} 
