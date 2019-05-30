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
  Override built-in stage step to be able to set a GitHub check as long as the
  call contains a second parameter.

  // New behaviour: a stage which will create a GitHub check called Foo
  stage('stageName', 'Foo') {
    // block
  }

  // Normal behaviour: a stage
  stage('stageName') {
    // block
  }
*/
def call(String name, Closure body) {
    steps.stage(name) {
        body()
    }
}

def call(String name, String check, Closure body) {
    githubNotify(context: check, description: "${name} ...", status: 'PENDING')
    try {
        call(name) {
            body()
        }
        githubNotify(context: check, description: "${name} passed", status: 'SUCCESS')
    } catch (err) {
        githubNotify(context: check, description: "${name} failed", status: 'FAILURE')
        throw err
    }
}

def call(Closure body) {
    error('Missing stage name')
}
