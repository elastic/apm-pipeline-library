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
 Grab the coverage files, and create the report in Jenkins.

 coverageReport("${WORKSPACE}/build")

*/

def call(baseDir) {
  publishHTML(target: [
    allowMissing: true,
    keepAll: true,
    reportDir: "${baseDir}",
    reportFiles: 'coverage-*-report.html',
    reportName: 'Coverage-Sourcecode-Files',
    reportTitles: 'Coverage'])
/*
  The current version does not show the coverage on the source code.
  publishCoverage(adapters: [
    coberturaAdapter("${baseDir}/coverage-*-report.xml")],
    sourceFileResolver: sourceFiles('STORE_ALL_BUILD'))
*/
  cobertura(autoUpdateHealth: false,
    autoUpdateStability: false,
    coberturaReportFile: "${baseDir}/coverage-*-report.xml",
    conditionalCoverageTargets: '70, 0, 0',
    failNoReports: false,
    failUnhealthy: false,
    failUnstable: false,
    lineCoverageTargets: '80, 0, 0',
    maxNumberOfBuilds: 0,
    methodCoverageTargets: '80, 0, 0',
    onlyStable: false,
    sourceEncoding: 'ASCII',
    zoomCoverageChart: false)
}
