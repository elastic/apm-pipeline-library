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

  Generate a report using the `id` script and compare the output
  with the `TARGET_BRANCH` variable if exists. Then it creates a report
  using the template `id`.

  generateReport(id: 'bundlesize', input: 'packages/rum/reports/apm-*-report.html', template: true, compare: true)

  NOTE: id is the identified for the script to be used, the report to be generated
        in json format and the template report to be generated.
**/

def call(Map args = [:]) {
  if(!isUnix()){
    error('generateReport: windows is not supported yet.')
  }
  def id = args.containsKey('id') ? args.id : error('generateReport: id param is required.')
  def input = args.containsKey('input') ? args.input : error('generateReport: input param is required.')
  def output = args.get('output', 'build')
  def template = args.get('template', true)
  def templateFormat = args.get('templateFormat', 'md')
  def compare = args.get('compare', true)
  
  // Prepare the compare context with.
  def compareWith = getCompareWithFileIfPossible(id: id, output: output, compare: compare)

  // Run the report generation script
  def scriptFile = "${id}.sh"
  def resourceContent = libraryResource("scripts/${scriptFile}")
  writeFile file: scriptFile, text: resourceContent
  sh(label: 'generateReport', script: """#!/bin/bash -x
                                    chmod 755 ${scriptFile}
                                    ./${scriptFile} "${id}" "${output}" "${input}" "${compareWith}" """)

  // Generate the template
  if(template){
    createFileFromTemplate(data: "${output}/${id}.json", template: "${id}.${templateFormat}.j2", output: "${output}/${id}.${templateFormat}", localTemplate: false)
    dir("${output}") {
      // This is required then the pipeline can use something like
      //      notifyBuildResult(newPRComment: [ "${id}": '"${id}.md" ])
      stash name: "${id}.${templateFormat}", includes: "${id}.${templateFormat}"
    }
  }

  // Archive the reports generated in the output folder with the id name
  dir("${output}") {
    archiveArtifacts(allowEmptyArchive: true, artifacts: "${id}.*", onlyIfSuccessful: false)
  }
}

def getCompareWithFileIfPossible(Map args = [:]) {
  def compareWith = ''
  if(args.compare) {
    // This is only available for Pull Requests
    if (env.CHANGE_TARGET){
      try {
        dir("${args.output}/${env.CHANGE_TARGET}") {
          projectName = env.JOB_NAME.replace(env.JOB_BASE_NAME, env.CHANGE_TARGET)
          copyArtifacts(filter: "${args.id}.json", flatten: true, optional: true, projectName: projectName, selector: lastWithArtifacts())
        }
      } catch(e) {
        log(level: 'INFO', text: 'generateReport: it was not possible to copy the previous build.')
      } finally {
        compareWith = "${args.output}/${env.CHANGE_TARGET}/${args.id}.json"
        if (!fileExists("${compareWith}")) {
          compareWith = ''
        }
      }
    }
  }
  return compareWith
}
