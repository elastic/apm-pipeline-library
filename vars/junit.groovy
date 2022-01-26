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
  Use the https://github.com/mdelapenya/junit2otel library to send traces and spands for a set of JUnit test reports

  junit(testResults: 'TEST-*.xml')
  junit(testResults: 'TEST-*.xml', serviceName: 'my-service')
  junit(testResults: 'TEST-*.xml', serviceName: 'my-service', serviceVersion: '1.2.3')
  junit(testResults: 'TEST-*.xml', serviceName: 'my-service', serviceVersion: '1.2.3', traceName: 'my-trace')

*/
def call(Map args = [:]) {
  def sendTraces = env.JUNIT_2_OTLP?.trim() ? true : false

  if (sendTraces) {
    log(level: 'INFO', text: 'Override default junit')

    def junitArgs = args

    if(!isUnix()){
      error('junit: windows is not supported yet.')
    }

    if(!isInstalled(tool: 'docker', flag: '--version')) {
      error('junit: docker is not installed but required.')
    }

    def testResults = junitArgs.containsKey('testResults') ? junitArgs.testResults : error("junit: testResults parameter is required.")
    def serviceName = junitArgs.containsKey('serviceName') ? junitArgs.serviceName : 'junit2otlp'
    def serviceVersion = junitArgs.containsKey('serviceVersion') ? junitArgs.serviceVersion : '0.0.0'
    def traceName = junitArgs.containsKey('traceName') ? junitArgs.traceName : 'junit2otlp'

    log(level: 'INFO', text: "Sending traces for '${serviceName}-${serviceVersion}-${traceName}'")

    withEnv([
      "SERVICE_NAME=${serviceName}",
      "SERVICE_VERSION=${serviceVersion}",
      "TEST_RESULTS_GLOB=${testResults}",
      "TRACE_NAME=${traceName}"
    ]){
      withOtelEnv() {
        try {
          sh(label: 'Run junit2otlp to send traces and metrics', script: libraryResource("scripts/junit2otel.sh"))
        }
        finally {
          junitArgs.remove('serviceName')
          junitArgs.remove('serviceVersion')
          junitArgs.remove('traceName')
        }
      }
    }
  }

  return steps.junit(junitArgs)
}
