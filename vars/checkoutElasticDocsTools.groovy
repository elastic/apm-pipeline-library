#!/usr/bin/env groovy

/**
  Checkout the tools to build documentation from the  https://github.com/elastic/docs.git repo.

  checkoutElasticDocsTools(basedir: 'folder')
*/
def call(Map params = [:]){
  def baseDir =  params.containsKey('basedir') ? params.basedir : "${env.WORKSPACE}/elastic/docs"
  dir("${baseDir}"){
    sh label: 'Checkout docs build tool', script: """#!/bin/bash
    set -euxo pipefail
    git init
    git remote add origin https://github.com/elastic/docs.git
    git config core.sparsecheckout true
    echo build_docs >> .git/info/sparse-checkout
    git fetch --no-tags --progress --depth=1 origin +refs/heads/master:refs/remotes/origin/master
    git checkout master
    git pull origin master
    """
  }
}
