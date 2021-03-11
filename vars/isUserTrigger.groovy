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
  Check if the build was triggered by a user.

  def userTrigger = isUserTrigger()
*/
def call(){
  def buildCause = currentBuild.getBuildCauses()?.find{ it._class == 'hudson.model.Cause$UserIdCause'}
  log(level: 'DEBUG', text: "isUserTrigger: ${buildCause?.userId?.toString()}")
  if (!buildCause || buildCause.userId instanceof net.sf.json.JSONNull) {
    return false
  }
  if (buildCause?.userId?.trim()) {
    env.BUILD_CAUSE_USER = buildCause?.userId
    return true
  }
  return false
}
