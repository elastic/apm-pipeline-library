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

 tar(file: 'archive.tgz', dir: '.', archive: true, allowMissing: true)
*/
def call(Map args = [:]) {
  def file = args.get('file', 'archive.tgz')
  def archive = args.get('archive', true)
  def dir = args.get('dir', '.')
  def allowMissing = args.get('allowMissing', true)

  // NOTE: pathPrefix is not required anymore since tar --exclude has been enabled 
  if (args.pathPrefix?.trim()) {
    log(level: 'WARN', text: 'tar: pathPrefix parameter is deprecated.')
  }

  try {
    compress(file: file, dir: dir)
    if(archive){
      archiveArtifacts(allowEmptyArchive: true, artifacts: file, onlyIfSuccessful: false)
    }
  } catch (e){
    log(level: 'INFO', text: "${file} was not compressed or archived : ${e?.message}")
    currentBuild.result = allowMissing ? 'SUCCESS' : 'UNSTABLE'
  }
}

def compress(Map args = [:]) {
  writeFile(file: "${args.file}", text: '')
  def command = "tar --exclude=${args.file} -czf ${args.file} ${args.dir}"
  if(isUnix()) {
    sh(label: 'Compress', script: command)
  } else {
    // Some CI Windows workers got the tar binary in the system32
    // As long as those are not defined in the PATH let's use this hack
    withEnv(["PATH+SYSTEM=C:\\Windows\\System32"]) {
      bat(label: 'Compress', script: command)
    }
  }
}
