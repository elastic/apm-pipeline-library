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
 This step parses the given list of branch aliases and return
 the branch name.

 This is handy to support a dynamic branch generation without the need to
 update the name of the branch when a new minor release branch is created.

 getBranchesFromAliases(aliases: ['main', '8.<minor>', '8.<next-minor>'])
*/

def call(Map args = [:]) {
  def aliases = args.containsKey('aliases') ? args.aliases : error('getBranchesFromAliases: aliases parameter is required')

  def branches = []
  // Expand macros and filter duplicated matches.
  aliases.each { alias ->
    def branchName = getBranchNameFromAlias(alias)
    if (!branches.contains(branchName)) {
      branches << branchName
    }
  }

  return branches
}

def getBranchNameFromAlias(alias) {
  // special macro to look for the latest minor version - subtrahend
  if (alias.contains('8.<minor-')) {
    def minorParts = alias.split('-')
    return subtract(bumpUtils.getMajorMinor(bumpUtils.getCurrentMinorReleaseFor8()), minorParts[1])
  }
  // special macro to look for the latest minor version
  if (alias.contains('8.<minor>')) {
    return bumpUtils.getMajorMinor(bumpUtils.getCurrentMinorReleaseFor8())
  }
  if (alias.contains('8.<next-minor>')) {
    return bumpUtils.getMajorMinor(bumpUtils.getNextMinorReleaseFor8())
  }
  // special macro to look for the latest minor version
  if (alias.contains('8.<next-patch>')) {
    return bumpUtils.getMajorMinor(bumpUtils.getNextPatchReleaseFor8())
  }
  if (alias.contains('7.<minor>')) {
    return bumpUtils.getMajorMinor(bumpUtils.getCurrentMinorReleaseFor7())
  }
  if (alias.contains('7.<next-minor>')) {
    return bumpUtils.getMajorMinor(bumpUtils.getNextMinorReleaseFor7())
  }
  return alias
}

def subtract(String branch, String subtrahend) {
  def parts = branch.split('\\.')
  def major = parts[0]
  if (parts.size() == 1) {
    return branch
  }
  def index = parseInt(subtrahend, 0)
  def minor = parseInt(parts[1], 0)
  def newMinor = (minor - index)
  if (newMinor < 0) {
    return branch
  }
  return major + "." + newMinor
}

def parseInt(String value, int defaultValue) {
  def ret = defaultValue
  try {
    ret = Integer.parseInt(value)
  } catch (Exception ex) {
    echo("parseInt: index will be considered as zero: " + ex)
  }
  return ret
}
