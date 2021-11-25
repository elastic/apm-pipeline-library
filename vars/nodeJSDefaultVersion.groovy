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
  Return the value in the file `.nvmrc`, or a default value.

  nodeJSDefaultVersion()
**/
def call(Map args = [:]) {
  def nodeDefaultVersion = defaultVersion()
  def found = ['.nvmrc', "${env.BASE_DIR}/.nvmrc"].find { fileExists(it) }
  if (found) {
    nodeDefaultVersion = readFile(file: found)?.trim()
  }
  return nodeDefaultVersion
}

def defaultVersion(){
  return '14.17.5'
}
