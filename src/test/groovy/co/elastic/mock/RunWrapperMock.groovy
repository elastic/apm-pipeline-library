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
 * Mock RunWrapper class.
 */
public class RunWrapperMock implements Serializable {

  def rawBuild
  def previousBuild
  def number
  def result
  def currentResult

  public RunWrapperMock(Map params = [:]) {
    this.rawBuild = params.rawBuild
    this.previousBuild = params.previousBuild
    this.number = params.number
    this.result = (params.result ?: 'UNKNOWN')
    this.currentResult = (params.result ?: 'UNKNOWN')
  }

  public RunWrapperMock getPreviousBuild() {
    return previousBuild
  }

  public boolean isBuilding() {
    return this.result.equals('RUNNING')
  }

}
