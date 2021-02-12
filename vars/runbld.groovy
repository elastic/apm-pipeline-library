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
 Populate the test output using the runbld approach.

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
                        junitAndStore(stashedTestReports: stashedTestReports)
                    }
                }
            }
        }
        post {
            always {
                // Process stashed test reports
                runbld(stashedTestReports: stashedTestReports, project: env.REPO)
            }
        }
    }
*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('runbld: windows is not supported yet.')
  }
  def project = args.containsKey('project') ? args.project : error('runbld: project parameter is required')
  def stashedTestReports = args.containsKey('stashedTestReports') ? args.stashedTestReports : error('runbld: stashedTestReports parameter is required')

  catchError(buildResult: 'SUCCESS', message: 'runbld post build action failed.') {
    if (stashedTestReports) {
      def jobName = isPR() ? "elastic+${project}+pull-request" : "elastic+${project}"
      dir("${env.BASE_DIR}") {
        stashedTestReports.each { k, v ->
          dir(k) {
            unstash(v)
          }
        }
      }
      sh(label: 'Process JUnit reports with runbld',
        script: """\
        cat >./runbld-test-reports <<EOF
        echo "Processing JUnit reports with runbld..."
        EOF
        /usr/local/bin/runbld ./runbld-test-reports --job-name ${jobName}
        """.stripIndent())  // stripIdent() requires '''/
    } else {
      log(level: 'WARN', text: 'runbld: stashedTestReports is empty.')
    }
  }
}
