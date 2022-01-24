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
Find the branch name for a stack version in the Artifacts API given the conditions to compare with.

The step supports passing a minor version, returning the branch name including that minor (i.e. 7.15), or passing a version token in the
'<minor>' format. This format supports passing an index, separated by the minus operator: '<minor-1>', which will retrieve the previous
version for the last minor. If the index overflows the number of the total existing minors, the first minor will be retrieved (i.e.
'<minor-1999>').

The more common use case is when there are two minor versions in development at the same time: 7.16 and 7.17

def branchName7minor = getBranchNameFromArtifactsAPI(branch: '7.<minor>')
def branchName7Zero = getBranchNameFromArtifactsAPI(branch: '7.0')
def branchName7Minor = getBranchNameFromArtifactsAPI(branch: '7.<minor>')
def branchName7Minor1 = getBranchNameFromArtifactsAPI(branch: '7.<minor-1>')
def branchName7Minor2 = getBranchNameFromArtifactsAPI(branch: '7.<minor-2>')
*/

def call(Map args = [:]) {
  def branch = args.containsKey('branch') ? args.branch : error('getBranchNameFromArtifactsAPI: branch parameter is required')

  // To store all the latest snapshot versions
  def latestVersions = artifactsApi(action: 'latest-versions')

  def parts = branch.split('\\.')
  def major = parts[0]
  def minor
  if (parts.size() > 1) {
    minor = parts[1]
    minor = minor.replaceAll("<", "")
    minor = minor.replaceAll(">", "")
  }

    // special macro to look for the latest minor version
  if (minor?.contains('-')) {
    def minorParts = minor.split('-')
    minor = minorParts[0]

    // parse second part of the minor token
    def index = minorParts[1]
    try {
      index = Integer.parseInt(index)
    } catch (Exception ex) {
      println("Index will be considered as zero: " + ex)
      index = 0
    }

    def matchingVersions = latestVersions.collect{ k,v -> k }.findAll { it ==~ /${major}\.\d+/ }.sort()

    int size = matchingVersions.size()

    def retIndex = size - index - 1
    if (retIndex < 0) {
      retIndex = 0 // min limit
    } else if (retIndex >= size) {
      retIndex = size - 1 // max limit
    }

    return matchingVersions.get(retIndex)
  }

  // special macro to look for the latest minor version
  if (minor.equals('minor')) {
    return latestVersions.collect{ k,v -> k }.findAll { it ==~ /${major}\.\d+/ }.sort().last()
  }

  return branch
}
