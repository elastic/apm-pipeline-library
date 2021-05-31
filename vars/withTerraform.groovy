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
Install Terraform from a GCS bucket and export environment variables to use it.

withTerraform(root: "${env.WORKSPACE}/bin", version: "0.15.3"){
 ...
}

*/
def call(Map args = [:], Closure body) {
  def terraform_root = args.containsKey('root') ? args.root : "${env.WORKSPACE}/bin"
  def terraform_version = args.containsKey('version') ? args.version : ""
  withEnv([
    "TERRAFORM_ROOT=${terraform_root}",
    "PATH+TERRAFORM=${terraform_root}/bin"
    ]){
      def script = libraryResource("scripts/install-terraform.sh")
      writeFile(file: "install-terraform.sh", text: script)
      sh(label: 'Install terraform', script: "sh install-terraform.sh ${terraform_root} ${terraform_version}")
      body()
  }
}
