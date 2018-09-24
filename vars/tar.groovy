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
  def pathPrefix = params.containsKey('pathPrefix') ? "-C " + params.pathPrefix : ""
  sh "tar -czf " + file + " " + dir + " " + pathPrefix
  if(archive){
    archiveArtifacts(allowEmptyArchive: true, 
                          artifacts: file, 
                          onlyIfSuccessful: false)
  }
}