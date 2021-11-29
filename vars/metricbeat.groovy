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
 This step run a metricbeat Docker container to grab the Host metrics in a single file.

 metricbeat(output: 'docker_logs.log')
 ...
 metricbeat.stop()
*/
def call(Map args = [:]) {
  start(args)
}

def call(Map args = [:], Closure body) {
  try{
    start(args)
    body()
  } finally {
    stop(args)
  }
}

def start(Map args = [:]) {
  def config = args.containsKey('config') ? args.config : "metricbeat_conf.yml"
  def es_secret = args.containsKey('es_secret') ? args.es_secret : error("metricbeat: The parameter es_secret is mandatory.")
  def image = args.containsKey('image') ? args.image : "docker.elastic.co/beats/metricbeat:7.15.2"
  def workdir = args.containsKey('workdir') ? args.workdir : pwd()
  def timeout = args.containsKey('timeout') ? args.timeout : "30"
  def configPath = "${workdir}/${config}"

  log(level: 'INFO', text: 'Running metricbeat Docker container')
  configuremetricbeat(configPath)
  dockerID = runBeat(es_secret, configPath, image)
  waitForBeat(dockerID)

  def json = [
    id: dockerID,
    config: config,
    image: image,
    workdir: workdir,
    timeout: timeout,
  ]

  writeJSON(file: "${workdir}/metricbeat_container_${env.NODE_NAME}.json", json: json)
}

def stop(Map args = [:]){
  def workdir = args.containsKey('workdir') ? args.workdir : pwd()
  def configFile = "${workdir}/metricbeat_container_${env.NODE_NAME}.json"
  if(!fileExists(configFile)){
    log(level: 'WARNING', text: 'There is no configuration file to stop metricbeat.')
    return
  }
  def stepConfig = readJSON(file: configFile)
  def timeout = args.containsKey('timeout') ? args.timeout : stepConfig.timeout

  log(level: 'INFO', text: 'Stopping metricbeat Docker container')
  sh(label: 'Stop metricbeat', script: """
    docker stop --time ${timeout} ${stepConfig.id} || echo "Exit code \$?"
  """)
}

def runBeat(es_secret, configPath, image){
  def secret = getVaultSecret(secret: es_secret)?.data
  withEnvMask(vars: [
      [var: "ES_URL", password: secret?.url],
      [var: "ES_USERNAME", password: secret?.user],
      [var: "ES_PASSWORD", password: secret?.password],
      [var: "CONFIG_PATH", password: configPath],
      [var: "DOCKER_IMAGE", password: image]
  ]){
    sh(label: 'Run metricbeat to grab host metrics', script: libraryResource("scripts/beats/run_metricbeat.sh"))
    return readFile(file: 'docker_id')?.trim()
  }
}

def waitForBeat(dockerID){
  writeFile(file: "wait_for_beat.sh", text: libraryResource("scripts/beats/wait_for_beat.sh"))
  sh(label: 'Wait for metricbeat', script: "chmod ugo+rx ./wait_for_beat.sh && ./wait_for_beat.sh ${dockerID}")
}

def configuremetricbeat(config){
  if(fileExists(config)){
    return
  }
  writeFile(file: config, text: libraryResource("scripts/beats/metricbeat.yml"))
}
