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
  This step will definne some enviroment variables and mask their content in the, it simplifies Declarative syntax
  console output

  withEnvMask(vars: [
      [var: "CYPRESS_user", password: user],
      [var: "CYPRESS_password", password: password],
      [var: "CYPRESS_kibanaUrl", password: kibanaURL],
      [var: "CYPRESS_elasticsearchUrl", password: elasticsearchURL],
      ]){
        sh(label: "Build tests", script: "npm install")
        sh(label: "Lint tests", script: "npm run format:ci")
        sh(label: "Execute Smoke Tests", script: "npm run test")
    }

  this replaces the following code

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs:[
      [var: "CYPRESS_user", password: user],
      [var: "CYPRESS_password", password: password],
      [var: "CYPRESS_kibanaUrl", password: kibanaURL],
      [var: "CYPRESS_elasticsearchUrl", password: elasticsearchURL],
    ]]){
    withEnv(
      "CYPRESS_user=${user}",
      "CYPRESS_password=${password}",
      "CYPRESS_kibanaUrl=${kibanaURL}",
      "CYPRESS_elasticsearchUrl=${elasticsearchURL}",
    ) {
      sh(label: "Build tests", script: "npm install")
      sh(label: "Lint tests", script: "npm run format:ci")
      sh(label: "Execute Smoke Tests", script: "npm run test")
    }
  }
*/

def call(Map params = [], Closure body){
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: params.vars]){
    def varsEnv = []
    params.vars.each{ item ->
      varsEnv += "${item.var}=${item.password}"
    }
    withEnv(varsEnv) {
        body()
    }
  }
}
