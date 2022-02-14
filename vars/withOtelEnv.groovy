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
  Configure the OpenTelemetry Jenkins context to run the body closure.

  withOtelEnv() {
    // block
  }

*/

def call(Map args = [:], Closure body) {
  String credentialsId = args.get('credentialsId', '')
  if (!otelHelper.isPluginInstalled()) {
    error('withOtelEnv: opentelemetry plugin is not available')
  }

  // In case the credentialsId argument was not passed, then let's use the
  // OpenTelemetry configuration to dynamically provide those details.
  if (!credentialsId?.trim()) {
    credentialsId = otelHelper.calculateCrendentialsId()
  }

  if (credentialsId?.trim()) {
    runBodyWithCredentials(credentialsId) {
      body()
    }
  } else {
    log(level: 'WARNING', text: 'withOtelEnv: opentelemetry plugin has been configured without any authentication method.')
    // In case the credentialsId argument was not passed and no way to gather those
    // details from the OpenTelemetry configuration then run the body without credentials
    runBodyWithEndpoint() {
      body()
    }
  }
}

def runBodyWithCredentials(credentialsId, Closure body) {
  // Then, mask and provide the environment variables.
  withCredentials([string(credentialsId: credentialsId, variable: 'OTEL_TOKEN_ID')]) {
    def otel_headers = env.OTEL_EXPORTER_OTLP_HEADERS ? "${env.OTEL_EXPORTER_OTLP_HEADERS} " : ''
    withEnvMask(vars: [
      [var: 'ELASTIC_APM_SECRET_TOKEN', password: env.OTEL_TOKEN_ID],
      [var: 'OTEL_EXPORTER_OTLP_HEADERS', password: "${otel_headers}authorization=Bearer ${env.OTEL_TOKEN_ID}"]
    ]) {
      runBodyWithEndpoint(){
        body()
      }
    }
  }
}

def runBodyWithEndpoint(Closure body) {
  def entrypoint = otelHelper.getEndpoint()

  // Opentelemetry Jenkins plugin version 0.19 already provides the TRACEPARENT env
  // variable, so let's support previous versions.
  def otelEnvs = []
  if (!env.TRACEPARENT) {
    otelEnvs = ["TRACEPARENT=00-${env.TRACE_ID}-${env.SPAN_ID}-01"]
  }

  def serviceName = otelHelper.getServiceName()
  if (serviceName?.trim()) {
    otelEnvs << "JENKINS_OTEL_SERVICE_NAME=${serviceName}"
  }

  withEnvMask(vars: [
    [var: 'ELASTIC_APM_SERVER_URL', password: entrypoint],
    [var: 'OTEL_EXPORTER_OTLP_ENDPOINT', password: entrypoint],
  ]) {
    withEnv(otelEnvs){
      body()
    }
  }
}
