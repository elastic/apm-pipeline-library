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

  Create a file given a Jinja template and the data in a JSON format

  // if the template to be used is the one in the shared library
  createFileFromTemplate(data: 'my-data.json', template: 'my-template.md.j2', output: 'file.md')

  // if the template to be used is another one in the local workspace
  createFileFromTemplate(data: 'my-data.json', template: 'src/foo/templates/my-template.md.j2', output: 'file.md', localTemplate: true)
**/

def call(Map args = [:]) {
  if(!isUnix()){
    error('createFileFromTemplate: windows is not supported yet.')
  }
  def data = args.containsKey('data') ? args.data : error('createFileFromTemplate: data param is required.')
  def template = args.containsKey('template') ? args.template : error('createFileFromTemplate: template param is required.')
  def output = args.containsKey('output') ? args.output : error('createFileFromTemplate: output param is required.')
  def localTemplate = args.get('localTemplate', false)
  
  if(!localTemplate) {
    writeFile file: "${template}", text: libraryResource("scripts/templates/${template}")
  }

  def scriptFile = 'processJinjaTemplate.py'
  def resourceContent = libraryResource("scripts/${scriptFile}")
  writeFile file: scriptFile, text: resourceContent
  sh(script: """#!/bin/bash +x
            pip install virtualenv
            virtualenv ".venv"
            source ".venv/bin/activate"
            pip install jinja2
            set -x
            chmod 755 ${scriptFile}
            ./${scriptFile} -f ${data} -t ${template} -o ${output} """,
     label: 'createTemplate')       
}
