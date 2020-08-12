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
 Install Go and mage and run some command in a pre-configured environment.

  withMageEnv(version: '1.14.2'){
    sh(label: 'Go version', script: 'go version')
  }

   withMageEnv(version: '1.14.2', pkgs: [
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       sh(label: 'Run mage',script: 'mage -version')
   }

}
**/

def call(Map args = [:], Closure body) {
  def pkgs = args.containsKey('pkgs') ? args.pkgs : []
  pkgs.addAll([
   "github.com/magefile/mage",
   "github.com/elastic/go-licenser",
   "golang.org/x/tools/cmd/goimports",
   "github.com/jstemmer/go-junit-report"
  ])
  args.pkgs = pkgs
  withGoEnv(args, body)
}
