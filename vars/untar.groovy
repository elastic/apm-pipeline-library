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
 Extract the given tar file in the given folder if any, othrewise in the
 current directory.

  untar(file: 'src.tgz', dir: 'src')
*/
def call(Map args = [:]) {
  def file = args.get('file', 'archive.tgz')
  def dir = args.get('dir', '.')
  def failNever = args.get('failNever', true)

  try {
    extract(file: file, dir: dir)
  } catch (e){
    log(level: 'INFO', text: "${file} was not extracted : ${e?.message}")
    if (failNever) {
      currentBuild.result = 'UNSTABLE'
    } else {
      error("untar: step failed with error ${e?.message}")
    }
  } finally {
    cleanup(file)
  }
}

def extract(Map args = [:]) {
  if (isTarInstalled()) {
    extractWithTar(args)
  } else {
    extractWith7z(args)
  }
}

def extractWithTar(Map args = [:]) {
  def outputTarFlagIfAny = args.dir?.trim() ? "-C ${args.dir}" : ''
  def mkdirFlag = isUnix() ? '-p' : ''
  def mkdirDirIfAny = args.dir?.trim() ? "mkdir ${mkdirFlag} ${args.dir} && " : ''
  def command = "${mkdirDirIfAny} tar ${outputTarFlagIfAny} -xpf ${args.file}"
  if(isUnix()) {
    sh(label: 'Extract', script: command)
  } else {
    // Some CI Windows workers got the tar binary in the system32
    // As long as those are not defined in the PATH let's use this hack
    withEnv(["PATH+SYSTEM=C:\\Windows\\System32"]) {
      bat(label: 'Extract', script: command)
    }
  }
}

def extractWith7z(Map args = [:]) {
  if(isUnix()) {
    // This particular scenario should not happen.
    error('untar: 7z is not supported yet. *Nix got tar installed.')
  }
  if (!is7zInstalled()) {
    installTools([[ tool: '7zip.portable', version: '19.0', provider: 'choco']])
  }
  def outputFlagIfAny = args.containsKey('dir') ? "-o'${args.dir}'" : ''
  withEnv(["PATH+CHOCO=C:\\ProgramData\\chocolatey\\bin"]) {
    bat(label: 'Extract', script: "7z x -tgzip -so ${args.file} | 7z x -si -ttar ${outputFlagIfAny}")
  }
}

def cleanup(String filename) {
  if (fileExists(filename)) {
    if(isUnix()) {
      sh(label: 'Cleanup', script: "rm ${filename}", returnStatus: true)
    } else {
      bat(label: 'Cleanup', script: "del ${filename}", returnStatus: true)
    }
  }
}

// This could be potentially refactored. TBD
def isTarInstalled() {
  return cmd(returnStatus: true, script: "tar --version ${redirectStdout()}") == 0
}

def is7zInstalled() {
  return cmd(returnStatus: true, script: "7z ${redirectStdout()}") == 0
}

def redirectStdout() {
  def value = isUnix() ? '>/dev/null' : '>NUL'
  return value
}
