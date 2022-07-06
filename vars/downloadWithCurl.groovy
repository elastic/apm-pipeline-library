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
 Download the given URL using CUrl, if possible.

 downloadWithCurl(url: 'https://....', output: 'gsutil.tar.gz')

*/
def call(Map args = [:]) {
  def url = args.containsKey('url') ? args.url : error('downloadWithCurl: url parameter is required')
  def output = args.containsKey('output') ? args.output : error('downloadWithCurl: output parameter is required')
  def flags = args.get('flags', '')
  if(isInstalled(tool: 'curl', flag: '--version')) {
    cmd(label: 'download tool', script: "curl -sSLo ${output} --retry 3 --retry-delay 2 --max-time 10 ${flags} ${url}")
    return true
  } else {
    log(level: 'WARN', text: 'downloadWithCurl: downloadWithCurl is not available.')
  }
  return false
}
