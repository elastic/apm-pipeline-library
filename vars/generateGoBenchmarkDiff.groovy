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

Generate a Go benchmark report by comparing the existing benchmark with
the `TARGET_BRANCH` variable if exists.

  generateGoBenchmarkDiff(current: 'bench.out', filter: 'exclude')
**/

def call(Map args = [:]) {
  if(!isUnix()){
    error('generateGoBenchmarkDiff: windows is not supported yet.')
  }
  def file = args.containsKey('file') ? args.file : error('generateGoBenchmarkDiff: file parameter is required.')
  def filter = args.get('filter', 'none')

  if (!fileExists("${file}")) {
    error('generateGoBenchmarkDiff: file does not exist.')
  }

  // Prepare the compare context with.
  def compareWith = getCompareWithFileIfPossible(file: file, output: 'build')

  // Run the report generation script
  if (compareWith?.trim()) {
    runBenchmarkDiff(filter: filter, file: file, compareWith: compareWith)
  }
}

def getCompareWithFileIfPossible(Map args = [:]) {
  def compareWith = ''
    // This is only available for Pull Requests
  if (env.CHANGE_TARGET) {
    try {
      dir("${args.output}/${env.CHANGE_TARGET}") {
        projectName = env.JOB_NAME.replace(env.JOB_BASE_NAME, env.CHANGE_TARGET)
        copyArtifacts(filter: "${args.file}", flatten: true, optional: true, projectName: projectName, selector: lastWithArtifacts())
      }
    } catch(e) {
      log(level: 'INFO', text: 'generateGoBenchmarkDiff: it was not possible to copy the previous build.')
    } finally {
      compareWith = "${args.output}/${env.CHANGE_TARGET}/${args.file}"
      if (!fileExists("${compareWith}")) {
        compareWith = ''
      }
    }
  }
  return compareWith
}

def runBenchmarkDiff(Map args = [:]) {
  def diffReport = getReportFileName()
  def flags = ''
  if (args.filter.equals('exclude')) {
    flags = "| grep -v 'all equal' | grep -v '~'"
  }
  withGoEnv(pkgs: [ "golang.org/x/perf/cmd/..." ] ) {
    sh(label: 'generateGoBenchmarkDiff', script: "benchstat ${args.file} ${args.compareWith} ${flags} | tee ${diffReport}")
  }
  archiveArtifacts(allowEmptyArchive: true, artifacts: diffReport, onlyIfSuccessful: false)
  // This is required then the pipeline can use something like
  //      notifyBuildResult(notifyGoBenchmarkComment: true)
  stash(name: "${diffReport}", includes: "${diffReport}")
}

def getReportFileName() {
  return 'bench.diff'
}
