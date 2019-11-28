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
  Abort any previously running builds as soon as a new build starts

  Further details: https://issues.jenkins-ci.org/browse/JENKINS-43353

  cancelPreviousRunningBuilds()
*/
import com.cloudbees.groovy.cps.NonCPS

def call(Map params = [:]) {
  def maxBuildsToSearch = params.get('maxBuildsToSearch', 20)
  log(level: 'INFO', text: "Number of builds to be searched ${maxBuildsToSearch}")
  b = currentBuild
  for (int i=0; i<maxBuildsToSearch; i++) {
    b = b.getPreviousBuild();
    if (b == null) break;
    rawBuild = getRawBuild(b)
    if (rawBuild.isBuilding()) {
      log(level: 'INFO', text: "Let's stop on-going build #${b.number}")
      rawBuild.doStop()
      setDescription(rawBuild, "Aborted from #${currentBuild.number}")
    }
    rawBuild = null // make null to keep pipeline serializable
  }
  b = null // make null to keep pipeline serializable
}

@NonCPS
def getRawBuild(b) {
  return rawBuild = b.rawBuild
}

@NonCPS
def setDescription(b, description) {
  b.setDescription(description)
}
