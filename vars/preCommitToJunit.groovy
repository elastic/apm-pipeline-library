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
  Parse the pre-commit log file and generates a junit report

  preCommitToJunit(input: 'pre-commit.log', output: 'pre-commit-junit.xml')
*/
def call(Map params = [:]) {
  def input = params.containsKey('input') ? params.input : error('preCommitToJunit: input parameter is required.')
  def output = params.containsKey('output') ? params.output : error('preCommitToJunit: output parameter is required.')

  def content = readFile(file: input)

  def id, status, message = '', inprogress = false
  def data = '<testsuite>'
  content.split('\n').each { line ->
    def matcher = line =~ '(.+)\\.(Passed|Skipped|Failed)$'
    if (matcher.find()) {
      if (id) {
        data += toJunit(id, status, message)
      }
      id = matcher.group(1).replaceAll(/[\W_&&[^\s]]/, '').replaceAll('\\.\\.\\.','').trim()
      status = matcher.group(2)
      message = ''
    } else {
      message += line + '\n'
      inprogress = true
    }
  }
  if (inprogress) {
    data += toJunit(id, status, message)
  }
  data += '</testsuite>'

  writeFile file: output, text: data, encoding: 'UTF-8'
}

def toJunit(String name, String status, String message) {
  String output = "<testcase classname=\"pre_commit.lint\" name=\"${name}\""
  if (status?.toLowerCase().equals('skipped')) {
    output += "><skipped message=\"skipped\"/><system-out><![CDATA[${message}]]></system-out></testcase>"
  } else if (status?.toLowerCase().equals('failed')) {
    output += "><error message=\"error\"/><system-out><![CDATA[${message}]]></system-out></testcase>"
  } else {
    output += " />"
  }
  return output
}
