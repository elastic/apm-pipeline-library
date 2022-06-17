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

Wrapper to request an input approval and wait for the outcome
It returns true or false

*/

def call(Map args = [:]) {
  def message = args.containsKey('message') ? args.message : error('prompt: message parameter is required.')
  def okTitle = args.get('okTitle', 'Yes')
  try {
    input(message: message, ok: okTitle)
    return true
  } catch(err) {
    return false
  }
}
