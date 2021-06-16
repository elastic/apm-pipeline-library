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
  Return the value of the variable GO_VERSION, the value in the file `.go-version`, or a default value.

  goDefaultVersion()
**/
def call(Map args = [:]) {
  def goDefaultVersion = defaultVersion()
  if(isGoVersionEnvVarSet()) {
    goDefaultVersion = "${env.GO_VERSION}"
  } else {
    def goFileVersion = '.go-version'
    if (fileExists(goFileVersion)){
      goDefaultVersion = readFile(file: goFileVersion)?.trim()
    }
  }
  return goDefaultVersion
}

def isGoVersionEnvVarSet(){
  return env.GO_VERSION != null && "" != "${env.GO_VERSION}"
}

def defaultVersion(){
  return '1.16.5'
}
