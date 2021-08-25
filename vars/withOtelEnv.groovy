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

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.GlobalConfiguration

def call(Map args = [:], Closure body) {
  String credentialsId = args.get('credentialsId', '')
  if (!isPluginInstalled(pluginName: 'opentelemetry')) {
    error('withOtelEnv: opentelemetry plugin is not available')
  }

  if (env.OTEL_EXPORTER_OTLP_HEADERS?.trim()) {
    error('withOtelEnv: OTEL_EXPORTER_OTLP_HEADERS env variable is already defined.')
  }

  def otelPlugin = getOtelPlugin()

  // In case the credentialsId argument was not passed, then let's use the
  // OpenTelemetry configuration to dynamically provide those details.
  if (!credentialsId?.trim()) {
    def otelAuthentication = otelPlugin.getAuthentication()
    if (otelAuthentication instanceof io.jenkins.plugins.opentelemetry.authentication.BearerTokenAuthentication) {
      credentialsId = otelAuthentication.getTokenId()
    }
  }

  // Then, mask and provide the environment variables.
  withCredentials([string(credentialsId: credentialsId, variable: 'OTEL_TOKEN_ID')]) {
    def entrypoint = otelPlugin.getEndpoint()
    withEnvMask(vars: [
      [var: 'ELASTIC_APM_SERVER_URL', password: entrypoint],
      [var: 'ELASTIC_APM_SECRET_TOKEN', password: env.OTEL_TOKEN_ID],
      [var: 'OTEL_EXPORTER_OTLP_ENDPOINT', password: entrypoint],
      [var: 'OTEL_SERVICE_NAME', password: otelPlugin.getServiceName()],
      [var: 'OTEL_EXPORTER_OTLP_HEADERS', password: "authorization=Bearer ${env.OTEL_TOKEN_ID}"]
    ]) {
      body()
    }
  }
}

@NonCPS
def getOtelPlugin() {
  return Jenkins.get().getExtensionList(GlobalConfiguration.class).get(io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration.class)
}
