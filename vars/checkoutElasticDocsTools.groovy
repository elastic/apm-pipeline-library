#!groovy

/**
  Checkout the tools to build documentation from the  https://github.com/elastic/docs.git repo.
  
  checkoutElasticDocsTools(basedir: 'folder')
*/
def call(Map params = [:]){
  def baseDir =  params.containsKey('basedir') ? params.basedir : "${env.WORKSPACE}/elastic/docs"
  dir("${baseDir}"){
    sh """#!/bin/bash
    set -euxo pipefail
    git init
    git remote add origin https://github.com/elastic/docs.git
    git config core.sparsecheckout true
    echo lib >> .git/info/sparse-checkout
    echo build_docs.pl >> .git/info/sparse-checkout
    echo .run >> .git/info/sparse-checkout
    echo conf.yaml >> .git/info/sparse-checkout
    echo resources >> .git/info/sparse-checkout
    echo shared >> .git/info/sparse-checkout
    git checkout master
    git pull origin master
    """
  }
}