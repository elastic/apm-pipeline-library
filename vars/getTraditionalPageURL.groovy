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
  Provides the specific traditional URL tab for the current build/run

  Tab refers to the kind of available pages in the traditional view. So far:
  * pipeline -> aka the build run (for BO compatibilities)
  * tests
  * changes
  * artifacts
  * cobertura
  * gcs

  def testURL = getTraditionalPageURL('tests')
  def artifactURL = getTraditionalPageURL('artifacts')

*/
def call(String page) {
  def url

  switch (page) {
    case 'artifacts':
      url = "${env.BUILD_URL}artifact"
      break
    case 'changes':
      url = "${env.BUILD_URL}changes"
      break
    case 'pipeline':
      url = "${env.BUILD_URL}"
      break
    case 'tests':
      url = "${env.BUILD_URL}testReport"
      break
    case 'cobertura':
      url = "${env.BUILD_URL}cobertura"
      break
    case 'gcs':
      url = "${env.BUILD_URL}gcsObjects"
      break
    default:
      error 'getTraditionalPageURL: Unsupported type'
  }
  return url
}
