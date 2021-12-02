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
  Check if the build was triggered by an upstream job, being possible to add a filter for the upstream cause.

  def upstreamTrigger = isUpstreamTrigger()
  def upstreamTrigger = isUpstreamTrigger(filter: 'PR-')
*/
def call(Map args=[:]){
  def filter = args.get('filter', 'all')

  def buildCause = currentBuild.getBuildCauses()?.find{ it._class == 'hudson.model.Cause$UpstreamCause' ||
                                                        it._class == 'org.jenkinsci.plugins.workflow.support.steps.build.BuildUpstreamCause' }
  if (buildCause?.upstreamProject?.trim()) {
    log(level: 'DEBUG', text: "isUpstreamTrigger: ${buildCause?.upstreamProject?.toString()}, filter: '${filter}'")
    // evaluate filter
    if (filter != 'all' ) {
      return buildCause?.upstreamProject?.toUpperCase()?.contains(filter.toUpperCase())
    }

    return true
  }
  return false
}
