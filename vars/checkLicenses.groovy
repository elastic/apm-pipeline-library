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

  checkLicenses(ext: '.groovy', exclude: './target', license: 'Elastic', licensor: 'Elastic A.B.')

*/
def call(Map params = [:]) {
  def excludeFlag = params.containsKey('exclude') ? "-exclude ${params.exclude}" : ''
  def fileExtFlag = params.containsKey('ext') ? "-ext ${params.ext}" : ''
  def licenseFlag = params.containsKey('license') ? "-license ${params.license}" : ''
  def licensorFlag = params.containsKey('licensor') ? "-licensor ${params.licensor}" : ''
  def skipFlag = params.get('skip') ? '-d' : ''

  docker.image('golang:1.12').inside("-e HOME=${env.WORKSPACE}/${env.BASE_DIR ?: ''}"){
    sh(label: 'Check Licenses', script: """
    go get -u github.com/elastic/go-licenser
    go-licenser ${skipFlag} ${excludeFlag} ${fileExtFlag} ${licenseFlag} ${licensorFlag}""")
  }
}
