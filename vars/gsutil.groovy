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
<<<<<<< HEAD
  def command = args.containsKey('command') ? args.command : error('gsutil: command argument is required.')
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : error('gsutil: credentialsId argument is required.')
  def gsUtilLocation = pwd(tmp: true)

  withEnv(["PATH+GSUTIL=${gsUtilLocation}", "PATH+GSUTIL_BIN=${gsUtilLocation}/bin"]) {
    if(!isInstalled(tool: 'gsutil', flag: '--version')) {
      downloadInstaller(gsUtilLocation)
    }

    withCredentials([file(credentialsId: credentialsId, variable: 'FILE_CREDENTIAL')]) {
      def credentialsVariable = isUnix() ? '${FILE_CREDENTIAL}' : '%FILE_CREDENTIAL%'
      cmd(label: 'authenticate', script: 'gcloud auth activate-service-account --key-file ' + credentialsVariable)
    }
=======
  def command = args.containsKey('command') ? args.command : error('gsutil: command parameter is required.')
  def credentialsId = args.containsKey('credentialsId') ? args.credentialsId : error('gsutil: credentialsId parameter is required.')

  withGCPEnv(credentialsId: credentialsId) {
>>>>>>> 292cd8a (Add withGCPEnv step (#1310))
    return cmd(label: "gsutil ${command}", script: "gsutil ${command}", returnStdout: true)
  }
}
