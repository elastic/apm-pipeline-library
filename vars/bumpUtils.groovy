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
  Utils class for the bump automation pipelines
*/

def areChangesToBePushed(String branch) {
  return sh(returnStatus: true, script: "git diff --quiet HEAD..${branch}") != 0
}

def createBranch(Map args = [:]) {
  sh(script: """git checkout -b "${args.prefix}-\$(date "+%Y%m%d%H%M%S")-${args.suffix}" """, label: 'Git branch creation')
}

def isVersionAvailable(stackVersion) {
  // pinned snapshot versions use -SNAPSHOT suffix.
  def version = stackVersion.endsWith('SNAPSHOT') ? stackVersion : "${stackVersion}-SNAPSHOT"
  return dockerImageExists(image: "docker.elastic.co/elasticsearch/elasticsearch:${version}")
}

def prepareContext(Map args = [:]) {
  deleteDir()
  setupAPMGitEmail(global: true)
  git(url: "https://github.com/${args.org}/${args.repo}.git",
      branch: args.branchName,
      credentialsId: args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'))
}

def parseArguments(Map args = [:]) {
  def arguments = [
      title: "${args.title}", labels: "${args.labels}", description: "${args.message}", base: "${args.branchName}"
  ]
  if (args.assign?.trim()) {
    arguments['assign'] = args.assign
  }
  if (args.reviewer?.trim()) {
    arguments['reviewer'] = args.reviewer
  }
  return arguments
}

def getMajor(String key) {
  def value = getValueForPropertyKey(key)
  def parts = value.split('\\.')
  if (parts.size() == 3) {
    return parts[0]
  }
  error('getMajor: version is not major.minor.patch formatted')
}

def getMajorMinor(String key) {
  def value = getValueForPropertyKey(key)
  def parts = value.split('\\.')
  if (parts.size() == 3) {
    return parts[0] + "." + parts[1]
  }
  error('getMajorMinor: version is not major.minor.patch formatted')
}

def getCurrentMinorReleaseFor8() {
  return getValueForPropertyKey(current8Key())
}

def getNextMinorReleaseFor8() {
  return getValueForPropertyKey(nextMinor8Key())
}

def getNextPatchReleaseFor8() {
  return getValueForPropertyKey(nextPatch8Key())
}

def getCurrentMinorReleaseFor7() {
  return getValueForPropertyKey(current7Key())
}

def getNextMinorReleaseFor7() {
  return getValueForPropertyKey(nextMinor7Key())
}

def getNextPatchReleaseFor7() {
  return getValueForPropertyKey(nextPatch7Key())
}

def getCurrentMinorReleaseFor6() {
  return getValueForPropertyKey(current6Key())
}

// for internal purposes
def getValueForPropertyKey(String key) {
  def stackVersions = readProperties(text: libraryResource('versions/releases.properties'))
  def version = stackVersions[key]
  if (!version?.trim()) {
    error("getValueForPropertyKey: '${key}' has an empty value")
  }
  return version
}

def current8Key() {
  return 'current_8'
}

def nextMinor8Key() {
  return 'next_minor_8'
}

def nextPatch8Key() {
  return 'next_patch_8'
}

def current7Key() {
  return 'current_7'
}

def nextMinor7Key() {
  return 'next_minor_7'
}

def nextPatch7Key() {
  return 'next_patch_7'
}

def current6Key() {
  return 'current_6'
}

def areStackVersionsAvailable(stackVersions) {
  // TODO: to support the 8.x branches
  return isVersionAvailable(stackVersions.get(current6Key())) &&
    isVersionAvailable(stackVersions.get(current7Key())) &&
    isVersionAvailable(stackVersions.get(nextMinor7Key())) &&
    isVersionAvailable(stackVersions.get(nextPatch7Key()))
}
