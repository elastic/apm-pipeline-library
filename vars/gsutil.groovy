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

def call(Map args = [:]) {
  def command = args.containsKey('command') ? args.command : error('gsutil: command argument is required.')
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : error('gsutil: credentialsId argument is required.')
  def gsUtilLocation = pwd(tmp: true)
  def gsUtilLocationWin = "${gsUtilLocation}/google-cloud-sdk"

  withEnv(["PATH+GSUTIL=${gsUtilLocation}", "PATH+GSUTIL_BIN=${gsUtilLocation}/bin",
           "PATH+GSUTILWIN=${gsUtilLocationWin}", "PATH+GSUTILWIN_BIN=${gsUtilLocationWin}/bin"]) {
    if(!isInstalled(tool: 'gsutil', flag: '--version')) {
      downloadInstaller(gsUtilLocation)
    }

    withCredentials([file(credentialsId: credentialsId, variable: 'FILE_CREDENTIAL')]) {
      def credentialsVariable = isUnix() ? '${FILE_CREDENTIAL}' : '%FILE_CREDENTIAL%'
      cmd(label: 'authenticate', script: 'gcloud auth activate-service-account --key-file ' + credentialsVariable)
    }
    return cmd(label: "gsutil ${command}", script: "gsutil ${command}", returnStdout: true)
  }
}

def downloadInstaller(where) {
  def url = googleCloudSdkURL()
  def tarball = "gsutil.${isUnix() ? 'tar.gz' : 'zip'}"

  dir(where) {
    if (!downloadWithWget(tarball, url)) {
      downloadWithCurl(tarball, url)
    }
    uncompress(tarball)
  }
}

def downloadWithWget(tarball, url) {
  if(isInstalled(tool: 'wget', flag: '--version')) {
    retryWithSleep(retries: 3, seconds: 5, backoff: true) {
      cmd(label: 'download gsutil', script: "wget -q -O ${tarball} ${url}")
    }
    return true
  } else {
    log(level: 'WARN', text: 'gsutil: wget is not available. gsutil will not be installed then.')
  }
  return false
}

def downloadWithCurl(tarball, url) {
  if(isInstalled(tool: 'curl', flag: '--version')) {
    cmd(label: 'download gsutil', script: "curl -sSLo ${tarball} --retry 3 --retry-delay 2 --max-time 10 ${url}")
  } else {
    log(level: 'WARN', text: 'gsutil: curl is not available. gsutil will not be installed then.')
  }
}

def googleCloudSdkURL() {
  def url = 'https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-319.0.0'
  def arch = is64() ? 'x86_64' : 'x86'
  if (isUnix()) {
    return "${url}-linux-${arch}.tar.gz"
  } else {
    // use the bundled python artifact to avoid issues with the existing python2 installation when running gsutil in some Windows versions
    return "${url}-windows-${arch}-bundled-python.zip"
  }
}

def uncompress(tarball) {
  if (isUnix()) {
    sh(label: 'untar gsutil', script: "tar -xpf ${tarball} --strip-components=1")
  } else {
    unzip(quiet: true, zipFile: tarball)
  }
}
