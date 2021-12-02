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
Find the oldest stack version given the condition to compare with.

def version = findOldestSupportedVersion(versionCondition: "^7.14.0")
*/

def call(Map args = [:]) {
  def versionCondition = args.containsKey('versionCondition') ? args.versionCondition : error('findOldestStackVersion: versionCondition parameter is required')
  if (versionCondition.indexOf('||') >= 0) {
    return handleOr(versionCondition)
  }
  def version = removeOperator(versionCondition)
  def response = httpRequest(url: 'https://artifacts-api.elastic.co/v1/versions?x-elastic-no-kpi=true')
  def availableVersions = readJSON(text: response)

  def parts = version.split('\\.')

  // If this is specifying a major or a minor only, check with the zero version.
  while (parts.size() < 3) {
    version += ".0"
    parts += "0"
  }

  def major = parts[0]
  def minor = parts[1]
  def patch = parts[2]

  // Use the snapshot if this is the last patch version.
  def nextPatch = major + "." + minor + "." + ((patch as Integer) + 1)
  def nextPatchExists = (
    availableVersions.versions.contains(nextPatch) ||
    availableVersions.versions.contains(nextPatch + "-SNAPSHOT")
  )
  def snapshotVersion = version + "-SNAPSHOT"
  if (!nextPatchExists && availableVersions.versions.contains(snapshotVersion)) {
    return snapshotVersion
  }

  // Use the version as is if it exists.
  if (availableVersions.versions.contains(version)) {
    return version
  }

  // Old minors may not be available in artifacts-api, if it is older
  // than the others in the same major, return the version as is.
  def older = true
  for (availableVersion in availableVersions.versions) {
    def availableParts = availableVersion.split('\\.')
    if (availableParts.size() < 2) {
      continue
    }
    def availableMajor = availableParts[0]
    def availableMinor = availableParts[1]
    if (major == availableMajor && minor > availableMinor) {
      older = false
      break
    }
  }
  if (older) {
    return version
  }

  // If no version has been found so far, try with the snapshot of the next version
  // in the current major.
  def majorSnapshot = major + ".x-SNAPSHOT"
  if (availableVersions?.aliases.contains(majorSnapshot)) {
    return majorSnapshot
  }

  // Otherwise, return it, whatever this is.
  return version
}

def removeOperator(String versionCondition) {
  if (versionCondition.startsWith('^')) {
    return versionCondition.substring(1)
  }
  if (versionCondition.startsWith('~')) {
    return versionCondition.substring(1)
  }
  if (versionCondition.startsWith('>=')) {
    return versionCondition.substring(2)
  }
  error('findOldestStackVersion: versionCondition supports only ^, ~ and >= operators')
}

def handleOr(String versionCondition) {
  def versionConditions = versionCondition.split('\\s*\\|\\|\\s*')
  if (versionConditions.size() == 0) {
    error('no conditions found in "'+versionCondition+'"')
  }

  def result = ""
  for (condition in versionConditions) {
    def candidate = call(versionCondition: condition)
    if (result == "" || candidate < result) {
      result = candidate
    }
  }

  return result
}
