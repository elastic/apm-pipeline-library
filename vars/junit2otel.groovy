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

1. If the REPO variable is set, but the OTEL_SERVICE_NAME is not, then REPO will be used as service name.
2. If the REPO variable is set, but the JUNIT_OTEL_TRACE_NAME is not, then REPO will be used as trace name.
3. If the JUNIT_OTEL_SERVICE_VERSION is not set, then it will use, in this particular order: pull-request ID, tag name and branch name. Else, it will use 'unknown'.

    pipeline {
        ...
        stages {
            stage(...) {
                post {
                    always {
                        // JUnit with OpenTelemetry traces
                        withEnv([
                          "JUNIT_2_OTLP=true",
                          "OTEL_SERVICE_NAME=apm-pipeline-library",
                          "JUNIT_OTEL_SERVICE_VERSION=main",
                          "JUNIT_OTEL_TRACE_NAME=junit-tests"
                        ]){
                          junit2otel(testResults: 'TEST-*.xml')
                        }

                        // JUnit with attributes inferred from Repository
                        withEnv([
                            "JUNIT_2_OTLP=true",
                            "REPO=apm-pipeline-library",
                        ]){
                            junit2otel(testResults: 'TEST-*.xml')
                        }
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
    if(!isUnix()){
      error('junit: windows is not supported yet.')
    }

    if(!isInstalled(tool: 'docker', flag: '--version')) {
      error('junit2otel: docker is not installed but required.')
    }

    def testResults = args.containsKey('testResults') ? args.testResults : error("junit2otel: testResults parameter is required.")
    def serviceName = env.OTEL_SERVICE_NAME?.trim() ? env.OTEL_SERVICE_NAME?.trim() : (env.REPO?.trim() ? env.REPO?.trim() : 'junit2otel')
    def serviceVersion = env.JUNIT_OTEL_SERVICE_VERSION?.trim() ? env.JUNIT_OTEL_SERVICE_VERSION?.trim() : calculateFallbackServiceVersion()
    def traceName = env.JUNIT_OTEL_TRACE_NAME?.trim() ? env.JUNIT_OTEL_TRACE_NAME?.trim() : (env.REPO?.trim() ? env.REPO?.trim() : 'junit2otel')

    withEnv([
      "SERVICE_NAME=${serviceName}",
      "SERVICE_VERSION=${serviceVersion}",
      "TEST_RESULTS_GLOB=${testResults}",
      "TRACE_NAME=${traceName}",
      "TRACEPARENT=00-${env.TRACE_ID}-${env.SPAN_ID}-01"
    ]){
      withOtelEnv() {
        log(level: 'INFO', text: "Sending traces for '${serviceName}-${serviceVersion}-${traceName}'")
        sh(label: 'Run junit2otlp to send traces and metrics', script: libraryResource("scripts/junit2otel.sh"))
      }
    }
  }

  return junit(args)
}

def calculateFallbackServiceVersion() {
  if (isPR()) {
    return env.CHANGE_ID;
  }

  if (isTag()) {
    return env.TAG_NAME;
  }

  if (isBranch()) {
    return env.BRANCH_NAME;
  }

  return 'unknown'
}
