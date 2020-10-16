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
Wrap the junit built-in step to archive the test reports that are going to be
populated later on with the runbld post build step.

    // This is required to store the stashed id with the test results to be digested with runbld
    import groovy.transform.Field
    @Field def stashedTestReports = [:]

    pipeline {
        ...
        stages {
            stage(...) {
                post {
                    always {
                        // JUnit with stashed reports
                        junitAndStore(stashedTestReports: stashedTestReports, id: 'test-stage-foo', ...)
                    }
                }
            }
        }
        ...
    }
*/
def call(Map args = [:]) {
  def stashedTestReports = args.containsKey('stashedTestReports') ? args.stashedTestReports : error('junitAndStore: stashedTestReports param is required')
  def testResults = args.containsKey('testResults') ? args.testResults : error('junitAndStore: testResults param is required')
  def junitArgs = args.findAll { k,v -> !(k.equals('id') || k.equals('stashedTestReports')) }
  junit(junitArgs)
  // args.id could be null in some cases, so let's use the currentmilliseconds
  def suffix = new java.util.Date().getTime()
  def stageName = args.id ? "${args.id?.replaceAll('[\\W]|_', '-')}-${suffix}" : "uncategorized-${suffix}"
  stash(includes: testResults, allowEmpty: true, name: stageName, useDefaultExcludes: true)
  stashedTestReports[stageName] = stageName
}
