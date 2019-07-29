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
  Use the elastic licenser

  checkLicenses()

  checkLicenses(ext: '.groovy')

  checkLicenses(skip: true, ext: '.groovy')

  checkLicenses(skip: true, junit: true, ext: '.groovy')

  checkLicenses(ext: '.groovy', exclude: './target', license: 'Elastic', licensor: 'Elastic A.B.')

*/
def call(Map params = [:]) {
  if(!isUnix()){
    error('checkLicenses: windows is not supported yet.')
  }
  def excludeFlag = params.containsKey('exclude') ? "-exclude ${params.exclude}" : ''
  def fileExtFlag = params.containsKey('ext') ? "-ext ${params.ext}" : ''
  def licenseFlag = params.containsKey('license') ? "-license ${params.license}" : ''
  def licensorFlag = params.containsKey('licensor') ? "-licensor \"${params.licensor}\"" : ''
  def skipFlag = params.get('skip', false) ? '-d' : ''
  def junitFlag = params.get('junit', false)
  def testOutput = 'test.out'

  if (junitFlag && !params.get('skip')) {
    error('checkLicenses: skip should be enabled when using the junit flag.')
  }

  docker.image('golang:1.12').inside("-e HOME=${env.WORKSPACE}/${env.BASE_DIR ?: ''}"){
    catchError {
      sh(label: 'Check Licenses', script: """
      go get -u github.com/elastic/go-licenser
      go-licenser ${skipFlag} ${excludeFlag} ${fileExtFlag} ${licenseFlag} ${licensorFlag} | tee ${testOutput}""")
    }

    // Potentially supported with https://github.com/elastic/go-licenser/issues/23
    if (junitFlag) {
      def warnings = readFile(file: testOutput)
      def warningsList = warnings.split('\n')
      def junitOutput = '<?xml version="1.0" encoding="UTF-8"?><testsuite name="licenses">'
      if (warningsList.size() < 1 || !warningsList[0]?.trim()){
        junitOutput += '<testcase/>'
      } else {
        warningsList.each {
          def rawWarning = it.split(':')[0]
          def fileName = rawWarning.substring(rawWarning.lastIndexOf('/') + 1)
          def filePath = rawWarning.replaceAll('/', '.').replaceFirst('^\\.','')
          junitOutput += """<testcase name="${fileName}" classname="${filePath}" time="0">
          <failure message="${it}"></failure></testcase>"""
        }
      }
      junitOutput += '</testsuite>'
      writeFile(file: 'test-results.xml', text: junitOutput)
      junit(keepLongStdio: true, testResults: 'test-results.xml')
    }
  }
}
