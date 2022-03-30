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
Given the project, release type and version it runs the release manager
*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('releaseManager: windows is not supported yet.')
  }
  def project = args.containsKey('project') ? args.project : error('releaseManager: project parameter is required.')
  def version = args.containsKey('version') ? args.version : error('releaseManager: version parameter is required.')
  def type = args.get('type', 'snapshot')
  def artifactsFolder = args.get('artifactsFolder', 'build/distribution')
  def outputFile = args.get('outputFile', 'release-manager-report.out')

  if (version.contains('-SNAPSHOT')) {
    error('releaseManager: version parameter cannot contain the suffix -SNAPSHOT.')
  }

  withEnv(["PROJECT=${project}", "TYPE=${type}", "VERSION=${version}", "FOLDER=${artifactsFolder}", "OUTPUT_FILE=${outputFile}"]) {
    getVaultSecret.readSecretWrapper {
      sh(label: 'Release Manager', script: libraryResource('scripts/release-manager.sh'))
    }
  }
}
