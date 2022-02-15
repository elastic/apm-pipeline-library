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

package co.elastic.mock

/**
 * Mock the otelHelper var
 */
class OtelHelperMock implements Serializable {

  private installed = true
  private configured = true
  public OtelHelperMock(boolean installed, boolean configured) {
    this.installed = installed
    this.configured = configured
  }
  public boolean isPluginInstalled() { return this.installed }
  public calculateCrendentialsId() {
    if (configured) {
      return 'otel-token'
    }
    return ''
  }
  public getOtelPlugin() { return 'otel-plugin' }
  public getAuthentication() { return 'otel-authentication' }
  public getEndpoint() { return 'otel-endpoint' }
  public getServiceName() { return 'otel-service' }
}
