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
    echo lib >> .git/info/sparse-checkout
    echo build_docs.pl >> .git/info/sparse-checkout
    echo .run >> .git/info/sparse-checkout
    echo conf.yaml >> .git/info/sparse-checkout
    echo resources >> .git/info/sparse-checkout
    echo shared >> .git/info/sparse-checkout
    git fetch --no-tags --progress --depth=1 origin +refs/heads/master:refs/remotes/origin/master
    git checkout master
    git pull origin master
    """
  }
}
