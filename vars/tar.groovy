#!/usr/bin/env groovy

/**
 Compress a folder into a tar file.
 
 tar(file: 'archive.tgz',
  archive: true,
  dir: '.'
  pathPrefix: '')
*/
def call(Map params = [:]) {
  def file = params.containsKey('file') ? params.file : 'archive.tgz'
  def archive = params.containsKey('archive') ? params.archive : true
  def dir = params.containsKey('dir') ? params.dir : "."
  def pathPrefix = params.containsKey('pathPrefix') ? "cd " + params.pathPrefix + " && " : ""
  sh pathPrefix + "tar -czf " + file + " " + dir
  if(archive){
    archiveArtifacts(allowEmptyArchive: true, 
                          artifacts: file, 
                          onlyIfSuccessful: false)
  }
}