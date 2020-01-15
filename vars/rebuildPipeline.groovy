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
  if (currentBuild?.previousBuild?.currentResult == 'FAILURE' &&
      currentBuild?.previousBuild?.previousBuild?.currentResult == 'FAILURE') {
    log(level: 'INFO', text: "rebuildPipeline: there are more than 2 previous build failures.")
    return
  }

  // Apply the rebuild only for the explicit pipelines.
  switch(env.JOB_NAME?.trim()) {
    case ~/.*apm-agent-dotnet-mbp.*/:
      apmAgentDotnet()
      break
    case ~/.*apm-agent-go-mbp.*/:
      apmAgentGo()
      break
    case ~/.*apm-agent-java-mbp.*/:
      apmAgentJava()
      break
    case ~/.*apm-agent-nodejs-mbp.*/:
      apmAgentNodejs()
      break
    case ~/.*apm-agent-php-mbp.*/:
      apmAgentPhp()
      break
    case ~/.*apm-agent-python-mbp.*/:
      apmAgentPython()
      break
    case ~/.*apm-agent-ruby-mbp.*/:
      apmAgentRuby()
      break
    case ~/.*apm-agent-rum-mbp.*/:
      apmAgentRum()
      break
    case ~/.*apm-integration-tests\/.*/:
      apmIntegrationTests()
      break
    case ~/.*apm-integration-tests-selector-mbp.*/:
      apmIntegrationTestsSelector()
      break
    case ~/.*linting-mbp\/.*/:
      apmLinting()
      break
    case ~/.*apm-pipeline-library-mbp.*/:
      apmPipelineLibrary()
      break
    case ~/.*apm-server-mbp.*/:
      apmServer()
      break
    case ~/.*opbeans.*-mbp.*/:
      opbeans()
      break
    case ~/it\/timeout\/parentstream/:
      testing()
      break
    default:
      log(level: 'INFO', text: "rebuildPipeline: unsupported job '${env.JOB_NAME}'")
      break
  }
}

def apmAgentDotnet() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch)])
}

def apmAgentGo() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'bench_ci', value: params.bench_ci),
                     booleanParam(name: 'test_ci', value: params.test_ci),
                     booleanParam(name: 'docker_test_ci', value: params.docker_test_ci),
                     string(name: 'GO_VERSION', value: params.GO_VERSION)])
}

def apmAgentJava() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'bench_ci', value: params.bench_ci),
                     booleanParam(name: 'test_ci', value: params.test_ci),
                     booleanParam(name: 'smoketests_ci', value: params.smoketests_ci),
                     string(name: 'MAVEN_CONFIG', value: params.MAVEN_CONFIG)])
}

def apmAgentNodejs() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'bench_ci', value: params.bench_ci),
                     booleanParam(name: 'test_edge_ci', value: params.test_edge_ci),
                     booleanParam(name: 'tests_ci', value: params.tests_ci),
                     booleanParam(name: 'tav_ci', value: params.tav_ci)])
}

def apmAgentPython() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'bench_ci', value: params.bench_ci),
                     booleanParam(name: 'tests_ci', value: params.tests_ci),
                     booleanParam(name: 'package_ci', value: params.package_ci)])
}

def apmAgentPhp() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false)
}

def apmAgentRuby() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'bench_ci', value: params.bench_ci)])
}

def apmAgentRum() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'saucelab_test', value: params.saucelab_test),
                     booleanParam(name: 'parallel_test', value: params.parallel_test),
                     booleanParam(name: 'bench_ci', value: params.bench_ci)])
}

def apmIntegrationTests() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [string(name: 'ELASTIC_STACK_VERSION', value: params.ELASTIC_STACK_VERSION),
                     string(name: 'BUILD_OPTS', value: params.BUILD_OPTS),
                     booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch)])
}

def apmIntegrationTestsSelector() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [string(name: 'ELASTIC_STACK_VERSION', value: params.ELASTIC_STACK_VERSION),
                     string(name: 'BUILD_OPTS', value: params.BUILD_OPTS),
                     string(name: 'GITHUB_CHECK_REPO', value: params.GITHUB_CHECK_REPO),
                     string(name: 'GITHUB_CHECK_NAME', value: params.GITHUB_CHECK_NAME),
                     string(name: 'AGENT_INTEGRATION_TEST', value: params.AGENT_INTEGRATION_TEST),
                     string(name: 'GITHUB_CHECK_SHA1', value: params.GITHUB_CHECK_SHA1)])
}

def apmLinting() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false)
}

def apmPipelineLibrary() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'make_release', value: params.make_release)])
}

def apmServer() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false,
        parameters: [booleanParam(name: 'Run_As_Master_Branch', value: params.Run_As_Master_Branch),
                     booleanParam(name: 'linux_ci', value: params.linux_ci),
                     booleanParam(name: 'test_ci', value: params.test_ci),
                     booleanParam(name: 'windows_ci', value: params.windows_ci),
                     booleanParam(name: 'intake_ci', value: params.intake_ci),
                     booleanParam(name: 'test_sys_env_ci', value: params.test_sys_env_ci),
                     booleanParam(name: 'bench_ci', value: params.bench_ci),
                     booleanParam(name: 'release_ci', value: params.release_ci),
                     booleanParam(name: 'kibana_update_ci', value: params.kibana_update_ci),
                     booleanParam(name: 'its_ci', value: params.its_ci),
                     string(name: 'DIAGNOSTIC_INTERVAL', value: params.DIAGNOSTIC_INTERVAL),
                     string(name: 'ES_LOG_LEVEL', value: params.ES_LOG_LEVEL)])
}

def opbeans() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false)
}

def testing() {
  build(job: env.JOB_NAME, propagate: false, quietPeriod: 1, wait: false)
}
