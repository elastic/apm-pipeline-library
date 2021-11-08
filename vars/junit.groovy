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
Wrap the junit built-in step to perform extra operations with the test reports.

    pipeline {
        ...
        stages {
            stage(...) {
                post {
                    always {
                        // JUnit on steroids
                        junit(callback: closure(), ...)
                    }
                }
            }
        }
        ...
    }
*/
def call(Map args = [:]) {
  def hasCallback = args.containsKey('callback') ? true : false
  def testResults = args.containsKey('testResults') ? args.testResults : error('junit: testResults parameter is required')
  def junitArgs = args.findAll { k,v -> !(k.equals('callback')) }

  junit(junitArgs)

  if (!hasCallback) {
      return
  }

  def callback = args.callback

  log(level: 'DEBUG', text: "junit: Running callback operations after junit")
  callback()
}
