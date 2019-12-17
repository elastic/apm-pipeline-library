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

def call() {

  // Let's avoid infinite loops
  if (currentBuild?.previousBuild?.previousBuild?.currentResult == 'FAILURE') {
    log(level: 'INFO', text: "rebuildPipeline: there are more than 2 previous build failures.")
    return
  }

  // Let's ensure the params are passed from the pipeline.
  if (!params?.find()) {
    log(level: 'INFO', text: "rebuildPipeline: params doesn't exist")
    return
  }

  // Apply the rebuild only for the explicit pipelines.
  switch(env.JOB_NAME?.trim()) {
    case ~/.*apm-agent-python-mbp.*/:
      apmAgentPython()
      break
    default:
      log(level: 'INFO', text: "rebuildPipeline: unsupported job '${env.JOB_NAME}'")
      break
  }
}

def apmAgentPython() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'bench_ci', value: params.bench_ci),
                     booleanParam(name: 'tests_ci', value: params.tests_ci),
                     booleanParam(name: 'package_ci', value: params.package_ci)])
}
