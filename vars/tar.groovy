#!/usr/bin/env groovy

/**
 Compress a folder into a tar file.
 
 tar(file: 'archive.tgz',
  archive: true,
  dir: '.'
  pathPrefix: '',
  allowMissing: true)
*/
def call(Map params = [:]) {
  def file = params.containsKey('file') ? params.file : 'archive.tgz'
  def archive = params.containsKey('archive') ? params.archive : true
  def dir = params.containsKey('dir') ? params.dir : "."
  def pathPrefix = params.containsKey('pathPrefix') ? "cd '" + params.pathPrefix + "' && " : ""
  def allowMissing = params.containsKey('allowMissing') ? params.allowMissing : true
  
  if(!isUnix()){
    echo "tar step is compatible only with unix systems"
    return
  }
  try {
    sh pathPrefix + "tar -czf '" + file + "' '" + dir + "'"
    if(archive){
      archiveArtifacts(allowEmptyArchive: true, 
                            artifacts: file, 
                            onlyIfSuccessful: false)
    }
  } catch (e){
    echo file + " was not compresesd or archived"
    if(!allowMissing){
        currentBuild.result = "UNSTABLE"
    }
  }
}