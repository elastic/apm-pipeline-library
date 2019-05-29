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
 Compress a folder into a tar file.

 tar(file: 'archive.tgz',
  archive: true,
  dir: '.'
  pathPrefix: '',
  allowMissing: true)
*/
def call(Map params = [:]) {
  def file = params.containsKey('file') ? params.file : 'archive.tgz'
  def archive = params.containsKey('archive') ? params.archive : true
  def dir = params.containsKey('dir') ? params.dir : "."
  def pathPrefix = params.containsKey('pathPrefix') ? "cd '" + params.pathPrefix + "' && " : ""
  def allowMissing = params.containsKey('allowMissing') ? params.allowMissing : true

  if(!isUnix()){
    log(level: 'INFO', text: "tar step is compatible only with unix systems")
    return
  }
  try {
    sh label: 'Generating tar file', script: "${pathPrefix} tar -czf '${WORKSPACE}/${file}' '${dir}'"
    if(archive){
      archiveArtifacts(allowEmptyArchive: true,
                      artifacts: file,
                      onlyIfSuccessful: false)
    }
  } catch (e){
    log(level: 'INFO', text: "${file} was not compresesd or archived : ${e?.message}")
    if(!allowMissing){
      currentBuild.result = "UNSTABLE"
    } else {
      currentBuild.result = "SUCCESS"
    }
  }
}
