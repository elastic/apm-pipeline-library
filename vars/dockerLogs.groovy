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
  Archive all the docker containers in the current context.

  // Archive all the docker logs in the current context
  dockerLogs()

  // Archive all the docker logs in the current context using the step name 'test'
  //  and the test/docker-compose.yml file
  dockerLogs(step: 'test', dockerCompose: 'test/docker-compose.yml')

  // Archive all the docker logs in the current context using the step name 'test',
  //  the test/docker-compose.yml file and fail if any errors when gathering the docker
  //  log files
  dockerLogs(step: 'test', dockerCompose: 'test/docker-compose.yml', failNever: false)

*/
def call(Map args = [:]){
  if(!isUnix()){
    error('dockerLogs: windows is not supported yet.')
  }

  def label = args.get('step', '')
  def dockerCompose = args.get('dockerCompose', 'docker-compose.yml')
  def failNever = args.get('failNever', true)

  def flag = failNever ? '|| true' : ''
  def normaliseLabel = normalise(label)

  def scriptFile = 'docker-logs.sh'
  writeFile file: scriptFile, text: libraryResource("scripts/${scriptFile}")
  sh(label: 'Permission', script: "chmod 755 ${scriptFile}")
  sh(label: 'Docker logs', script: """./${scriptFile} "${normaliseLabel}" "${dockerCompose}" ${flag}""")

  archiveArtifacts(allowEmptyArchive: true, artifacts: 'docker-info/**', defaultExcludes: false)  
}

def normalise(value) {
  return value.replace(';','/').replace('--','_').replace('.','_').replace(' ','_')
}
