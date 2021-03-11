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
 This step run a filebeat Docker container to grab the Docker containers logs ina single file.

 filebeat(output: 'docker_logs.log')
 ...
 filebeat.stop()
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
  def sanitizedOutput = ''
  if (args.containsKey('output')) {
    sanitizedOutput = sanitizeOutputFileName(args.output)
  }
  sanitizedOutput = sanitizedOutput?.trim() ? sanitizedOutput : 'docker_logs.log'

  def output = sanitizedOutput
  def config = args.containsKey('config') ? args.config : "filebeat_conf.yml"
  def image = args.containsKey('image') ? args.image : "docker.elastic.co/beats/filebeat:7.10.1"
  def workdir = args.containsKey('workdir') ? args.workdir : pwd()
  def timeout = args.containsKey('timeout') ? args.timeout : "30"
  def archiveOnlyOnFail = args.get('archiveOnlyOnFail', false)
  def configPath = "${workdir}/${config}"

  log(level: 'INFO', text: 'Running Filebeat Docker container')

  configureFilebeat(configPath, output)
  def dockerID = sh(label: 'Run filebeat to grab container logs', script: """
    docker run \
      --detach \
      -v ${workdir}:/output \
      -v ${configPath}:/usr/share/filebeat/filebeat.yml \
      -u 0:0 \
      -v /var/lib/docker/containers:/var/lib/docker/containers \
      -v /var/run/docker.sock:/var/run/docker.sock \
      ${image} \
        --strict.perms=false \
        -environment container \
        -E http.enabled=true
  """, returnStdout: true)?.trim()
  sh(label: 'Wait for Filebeat', script: """
    N=0
    until docker exec ${dockerID} \
      curl -sSfI --retry 10 --retry-delay 5 --max-time 5 'http://localhost:5066/stats?pretty'
    do
      sleep 5
      if [ \${N} -gt 6 ]; then
        break;
      fi
      N=\$((\${N} + 1))
    done
  """)

  def json = [
    id: dockerID,
    output: output,
    config: config,
    image: image,
    timeout: timeout,
    archiveOnlyOnFail: archiveOnlyOnFail
  ]

  writeJSON(file: "${workdir}/filebeat_container_${env.NODE_NAME}.json", json: json)
}

/**
* This method:
*  1. removes leading and trailing whitespaces, using trim()
*  2. replaces inner whitespaces with '_', using a regex
*  3. replaces any occurrences of non-characters or numbers or dots, with a '_', using a regex
*
* For simplicity, it could lead to collitions for different inputs: as an example,
* both "a&b" and "a|b" will result in "a_b".
* 
* @param input the string to be sanitised
* @return the sanitised ouput
*/
def String sanitizeOutputFileName(String input){
  return input?.trim().replaceAll("\\s","_").replaceAll("[^a-zA-Z0-9.]+", "_")
}

def stop(Map args = [:]){
  def workdir = args.containsKey('workdir') ? args.workdir : pwd()
  def stepConfig = readJSON(file: "${workdir}/filebeat_container_${env.NODE_NAME}.json")
  def timeout = args.containsKey('timeout') ? args.timeout : stepConfig.timeout
  def archiveOnlyOnFail = stepConfig.get('archiveOnlyOnFail', false)

  log(level: 'INFO', text: 'Stopping Filebeat Docker container')

  // we need to change the permission because the group others never will have permissions
  // due to the harcoded creation mask, see https://github.com/elastic/beats/issues/20584
  sh(label: 'Stop filebeat', script: """
    docker exec -t ${stepConfig.id} chmod -R ugo+rw /output || echo "Exit code \$?"
    docker stop --time ${timeout} ${stepConfig.id} || echo "Exit code \$?"
  """)

  if(!archiveOnlyOnFail || isBuildFailure()){
    archiveArtifacts(artifacts: "./${stepConfig.output}*", allowEmptyArchive: true)
  }
}

def configureFilebeat(config, output){
  if(fileExists(config)){
    return
  }
  def configuration = """
---
filebeat.autodiscover:
  providers:
    - type: docker
      templates:
        - config:
          - type: container
            paths:
              - /var/lib/docker/containers/\${data.docker.container.id}/*.log
processors:
  - add_host_metadata: ~
  - add_cloud_metadata: ~
  - add_docker_metadata: ~
  - add_kubernetes_metadata: ~
output.file:
  path: "/output"
  filename: ${output}
  permissions: 0644
  codec.format:
    string: '[%{[container.name]}][%{[container.image.name]}][%{[container.id]}][%{[@timestamp]}] %{[message]}'
"""
  writeFile(file: config, text: configuration)
}
