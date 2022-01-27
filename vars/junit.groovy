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
Wrap the junit built-in step to send OpenTelemetry traces for the test reports that are going to be
populated later on, using the https://github.com/mdelapenya/junit2otel library.

    pipeline {
        ...
        stages {
            stage(...) {
                post {
                    always {
                        // JUnit with OPenTelemetry traces
                        junit(testResults: 'TEST-*.xml')
                        junit(testResults: 'TEST-*.xml', serviceName: 'my-service')
                        junit(testResults: 'TEST-*.xml', serviceName: 'my-service', serviceVersion: '1.2.3')
                        junit(testResults: 'TEST-*.xml', serviceName: 'my-service', serviceVersion: '1.2.3', traceName: 'my-trace')
                    }
                }
            }
        }
        ...
    }

*/
def call(Map args = [:]) {
  def sendTraces = env.JUNIT_2_OTLP?.trim() ? true : false

  if (sendTraces) {
    log(level: 'INFO', text: 'Override default junit')

    if(!isUnix()){
      error('junit: windows is not supported yet.')
    }

    if(!isInstalled(tool: 'docker', flag: '--version')) {
      error('junit: docker is not installed but required.')
    }

    def testResults = args.containsKey('testResults') ? args.testResults : error("junit: testResults parameter is required.")
    def serviceName = env.OTEL_SERVICE_NAME?.trim() ? env.OTEL_SERVICE_NAME?.trim() : 'junit2otlp'
    def serviceVersion = env.JUNIT_OTEL_SERVICE_VERSION?.trim() ? env.JUNIT_OTEL_SERVICE_VERSION?.trim() : '0.0.0'
    def traceName = env.JUNIT_OTEL_TRACE_NAME?.trim() ? env.JUNIT_OTEL_TRACE_NAME?.trim() : 'junit2otlp'

    withEnv([
      "SERVICE_NAME=${serviceName}",
      "SERVICE_VERSION=${serviceVersion}",
      "TEST_RESULTS_GLOB=${testResults}",
      "TRACE_NAME=${traceName}"
    ]){
      withOtelEnv() {
        log(level: 'INFO', text: "Sending traces for '${serviceName}-${serviceVersion}-${traceName}'")
        sh(label: 'Run junit2otlp to send traces and metrics', script: libraryResource("scripts/junit2otel.sh"))
      }
    }
  }

  return steps.junit(args)
}
