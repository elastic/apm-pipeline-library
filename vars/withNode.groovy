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
Wrap the node call for three reasons:
  1. with some latency to avoid the known issue with the scalability in gobld.
  2. enforce one shoot ephemeral workers with the extra/uuid label that gobld provides.
  3. allocate a new workspace to workaround the flakiness of windows workers with deleteDir.

  // Use the ARM workers without any sleep or workspace allocation.
  withNode(labels: 'arm',){
    ...
  }

  // Use ephemeral worker with a sleep of up to 100 seconds and with a specific workspace.
  withNode(labels: 'immutable && ubuntu-18', sleepMax: 100, forceWorspace: true){
    ...
  }
*/
def call(Map args = [:], Closure body) {
  def sleepMin = args.get('sleepMin', 0)
  def sleepMax = args.get('sleepMax', 0)
  def labels = args.containsKey('labels') ? args.labels : error('withNode: labels parameter is required.')
  def forceWorkspace = args.get('forceWorkspace', false)
  def uuid = UUID.randomUUID().toString()

  // Sleep to smooth the ram up with the provisioner
  sleep(randomNumber(min: sleepMin, max: sleepMax))

  // In case of ephemeral workers then use the uuid
  def newLabels = isStaticWorker(labels: labels) ? labels : (labels?.trim() ? "${labels} && extra/${uuid}" : "extra/${uuid}")
  log(level: 'INFO', text: "Allocating a worker with the labels '${newLabels}'.")

  node("${newLabels}") {
    if(forceWorkspace) {
      // Allocate a new workspace
      ws(getWorkspace(uuid)) {
        body()
      }
    } else {
      body()
    }
  }
}

def getWorkspace(uuid) {
  // Ensure this step can be used outside of the multibranch pipeline item.
  def jobName = env.JOB_BASE_NAME ?: 'unknown'
  def buildNumber = env.BUILD_NUMBER ?: 'unknown'
  return "workspace/${jobName}-${buildNumber}-${uuid}"
}
