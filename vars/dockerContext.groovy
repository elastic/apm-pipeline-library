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
  Fetch the docker environment in the current context using filebeat and metricbeat

  // Archive all the docker logs in the current context
  dockerContext(filebeatOutput: 'logs.txt', metricbeatOutput: 'health.txt') {
    //
  }

*/
def call(Map args = [:], Closure body){
  if(!isUnix()){
    error('dockerContext: windows is not supported yet.')
  }

  def filebeatOutput = args.get('filebeatOutput', 'docker-filebeat.log')
  def metricbeatOutput = args.get('metricbeatOutput', 'docker-metricbeat.log')
  def archiveOnlyOnFail = args.get('archiveOnlyOnFail', false)

  filebeat(output: filebeatOutput, archiveOnlyOnFail: archiveOnlyOnFail){
    metricbeat(output: metricbeatOutput, archiveOnlyOnFail: archiveOnlyOnFail){
      body()
    }
  }
}
