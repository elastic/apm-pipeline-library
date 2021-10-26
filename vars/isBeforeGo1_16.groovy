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
  If the given Golang version is pre 1.16.

  whenTrue(isBeforeGo1_16(version: '1.17')) {
    ...
  }
*/
def call(Map args = [:]){
  def version = args.containsKey('version') ? args.version : goDefaultVersion()
  version = version.startsWith('go') ? version.minus('go') : version
  def ref = "1.16"
  def vVer = version.tokenize(".")
  def vRef = ref.tokenize(".")
  def n = Math.min(vVer.size(), vRef.size())
  for (int i = 0; i < n; i++) {
    def q = vVer[i].toInteger()
    def r = vRef[i].toInteger()
    if (q != r) {
      return q < r
    }
  }
  return vVer.size() < vRef.size()
}
