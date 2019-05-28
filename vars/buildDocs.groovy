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
  Build documentation from asciidoc files.

  buildDocs()
  buildDocs(docsDir: "docs", archive: true)
*/
def call(Map params = [:]){
  def docsDir =  params.containsKey('docsDir') ? params.docsDir : 'docs'
  def archive =  params.containsKey('archive') ? params.archive : false

  def elasticDocsDir = "${env.WORKSPACE}/elastic/docs"
  def builDocScript = libraryResource('scripts/jenkins/build_docs.sh')
  writeFile(file: 'build_docs.sh', text: builDocScript)
  sh label: 'Set script permissions', script: 'chmod ugo+rx build_docs.sh'

  checkoutElasticDocsTools(basedir: elasticDocsDir)
  withEnv(["ELASTIC_DOCS=${elasticDocsDir}"]){
    sh label: 'Build Docs', script: "./build_docs.sh ${docsDir}"
  }
  if(archive){
    tar(file: "doc-files.tgz", archive: true, dir: "html", pathPrefix: "${docsDir}")
  }
}
