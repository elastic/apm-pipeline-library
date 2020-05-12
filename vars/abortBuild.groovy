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

import com.cloudbees.groovy.cps.NonCPS

/**
  Abort the given build with the given message

  abortBuild(build: currentBuild)
*/

def call(Map params = [:]){
  def build = params.containsKey('build') ? params.build : error('abortBuild: build params is required')
  def message = params.get('message', 'Force to abort the build')
  def rawBuild = getRawBuild(build)
  if (build && rawBuild) {
    if (rawBuild.isBuilding()) {
      log(level: 'INFO', text: "Let's stop build #${build.number}. ${message}")
      rawBuild.doStop()
      setDescription(rawBuild, message)
    }
    rawBuild = null          // make null to keep pipeline serializable
    build = null             // make null to keep pipeline serializable
  } else {
    log(level: 'WARN', text: 'Build or rawBuild do not have any valid value')
  }
}

@NonCPS
def getRawBuild(b) {
  return b?.rawBuild
}

@NonCPS
def setDescription(b, description) {
  b.setDescription(description)
}
