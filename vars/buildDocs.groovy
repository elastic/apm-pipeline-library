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
  sh 'chmod ugo+rx build_docs.sh'

  checkoutElasticDocsTools(basedir: elasticDocsDir)
  withEnv(["ELASTIC_DOCS=${elasticDocsDir}"]){
    sh "build_docs.sh ${docsDir}"
  }
  if(archive){
    tar(file: "doc-files.tgz", archive: true, dir: "${docsDir}/html", pathPrefix: "${docsDir}")
  }
}
