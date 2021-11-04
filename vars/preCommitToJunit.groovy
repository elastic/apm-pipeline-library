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

import com.cloudbees.groovy.cps.NonCPS

/**
  Parse the pre-commit log file and generates a junit report

  preCommitToJunit(input: 'pre-commit.log', output: 'pre-commit-junit.xml')
*/
def call(Map args = [:]) {
  def input = args.containsKey('input') ? args.input : error('preCommitToJunit: input parameter is required.')
  def output = args.containsKey('output') ? args.output : error('preCommitToJunit: output parameter is required.')
  def reportSkipped = args.get('reportSkipped', false)

  def content = readFile(file: input)
  def id, status, message = '', inprogress = false
  def data = '<?xml version="1.0" encoding="UTF-8"?><testsuite>'
  content.split('\n').each { line ->
    def matcher = findPatternMatch(line, '(.+)(\\.Passed|\\)Skipped|\\.Skipped|\\.Failed)$')
    if (matcher.find()) {
      if (id) {
        data += toJunit(id, status, message, reportSkipped)
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
    data += toJunit(id, status, message, reportSkipped)
  }
  data += '</testsuite>'

  writeFile file: output, text: data, encoding: 'UTF-8'
}

def toJunit(String name, String status, String message, reportSkipped=false) {
  String output = "<testcase classname=\"pre_commit.lint\" name=\"${name}\""
  if (status?.toLowerCase()?.contains('skipped') && reportSkipped) {
    output += "><skipped message=\"skipped\"/><system-out><![CDATA[${normalise(message)}]]></system-out></testcase>"
  } else if (status?.toLowerCase()?.contains('failed')) {
    // use error and failure to populate the errorStackTrace when using resources/scripts/generate-build-data.sh
    output += "><error message=\"error\">![CDATA[${normaliseXml(normalise(message))}]]</error></testcase>"
  } else {
    output += " />"
  }
  return output
}

// See https://stackoverflow.com/questions/4237625/removing-invalid-xml-characters-from-a-string-in-java
def normalise(String message) {
  String xml10pattern = "[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff]"
  return message.replaceAll(xml10pattern, '')
}

def normaliseXml(String message) {
  return message.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('&', '&amp;')
}

@NonCPS
private findPatternMatch(line, pattern) {
  def matcher = line =~ pattern
  return matcher
}
