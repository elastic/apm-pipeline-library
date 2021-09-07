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
  Helper method to interact with the OpenTelemetry Jenkins plugin
*/

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.GlobalConfiguration

def isPluginInstalled() {
  return isPluginInstalled(pluginName: 'opentelemetry')
}

@NonCPS
def getOtelPlugin() {
  def value = Jenkins.get().getExtensionList(GlobalConfiguration.class).get(io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration.class)
  return value
}

@NonCPS
def calculateCrendentialsId() {
  def otelAuthentication = getAuthentication()
  if (otelAuthentication instanceof io.jenkins.plugins.opentelemetry.authentication.BearerTokenAuthentication) {
    return otelAuthentication.getTokenId()
  }
}

@NonCPS
def getAuthentication() {
  def value = getOtelPlugin().getAuthentication()
  return value
}

@NonCPS
def getEndpoint() {
  def value = getOtelPlugin().getEndpoint()
  return value
}
