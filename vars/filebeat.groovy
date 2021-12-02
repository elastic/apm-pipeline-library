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
 This step run a filebeat Docker container to grab the Docker containers logs in a single file.

 filebeat(output: 'docker_logs.log')
 ...
 filebeat.stop()
*/
def call(Map args = [:]) {
  start(args)
}

def call(Map args = [:], Closure body) {
  def ret = false
  try{
    start(args)
    body()
    ret = true
  } finally {
    args.isBuildSuccess = ret
    stop(args)
  }
}

def start(Map args = [:]) {
  def output = args.containsKey('output') ? args.output : 'docker_logs.log'
  def config = args.containsKey('config') ? args.config : "filebeat_conf.yml"
  def image = args.containsKey('image') ? args.image : "docker.elastic.co/beats/filebeat:7.15.2"
  def workdir = args.containsKey('workdir') ? args.workdir : pwd()
  def timeout = args.containsKey('timeout') ? args.timeout : "30"
  def archiveOnlyOnFail = args.get('archiveOnlyOnFail', false)
  def configPath = "${workdir}/${config}"

  log(level: 'INFO', text: 'Running Filebeat Docker container')

  configureFilebeat(configPath)
  def dockerID = runBeat(workdir, configPath, output, image)
  waitForBeat(dockerID)

  def json = [
    id: dockerID,
    output: output,
    config: config,
    image: image,
    workdir: workdir,
    timeout: timeout,
    archiveOnlyOnFail: archiveOnlyOnFail
  ]

  writeJSON(file: "${workdir}/filebeat_container_${env.NODE_NAME}.json", json: json)
}

def stop(Map args = [:]){
  def workdir = args.containsKey('workdir') ? args.workdir : pwd()
  def isBuildFailure = args.containsKey('isBuildSuccess') ? args.isBuildSuccess == false : isBuildFailure()
  def configFile = "${workdir}/filebeat_container_${env.NODE_NAME}.json"
  if(!fileExists(configFile)){
    log(level: 'WARNING', text: 'There is no configuration file to stop filebeat.')
    return
  }
  def stepConfig = readJSON(file: configFile)
  def timeout = args.containsKey('timeout') ? args.timeout : stepConfig.timeout
  def archiveOnlyOnFail = stepConfig.get('archiveOnlyOnFail', false)

  log(level: 'INFO', text: 'Stopping Filebeat Docker container')

  // we need to change the permission because the group others never will have permissions
  // due to the hardcoded creation mask, see https://github.com/elastic/beats/issues/20584
  sh(label: 'Stop filebeat', script: """
    docker exec -t ${stepConfig.id} chmod -R ugo+rw /output || echo "Exit code \$?"
    docker stop --time ${timeout} ${stepConfig.id} || echo "Exit code \$?"
  """)
  log(level: 'DEBUG', text: "archiveOnlyOnFail: ${archiveOnlyOnFail} - isBuildFailure: ${isBuildFailure}")
  if(archiveOnlyOnFail == false || (archiveOnlyOnFail && isBuildFailure)){
    log(level: 'DEBUG', text: 'filebeat: Archiving Artifacts')
    archiveArtifacts(artifacts: "**/${stepConfig.output}*", allowEmptyArchive: true)
  }
}

def runBeat(workdir, configPath, output, image){
  withEnv([
    "CONFIG_PATH=${configPath}",
    "DOCKER_IMAGE=${image}",
    "OUTPUT_DIR=${workdir}",
    "OUTPUT_FILE=${output}"
  ]){
    sh(label: 'Run filebeat to grab host metrics', script: libraryResource("scripts/beats/run_filebeat.sh"))
    return readFile(file: 'docker_id')?.trim()
  }
}

def waitForBeat(dockerID){
  writeFile(file: "wait_for_beat.sh", text: libraryResource("scripts/beats/wait_for_beat.sh"))
  sh(label: 'Wait for filebeat', script: "chmod ugo+rx ./wait_for_beat.sh && ./wait_for_beat.sh ${dockerID}")
}

def configureFilebeat(config){
  if(fileExists(config)){
    return
  }
  writeFile(file: config, text: libraryResource("scripts/beats/filebeat.yml"))
}
