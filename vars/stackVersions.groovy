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
  Return the version currently used for testing.

  stackVersions() // [ '8.0.0', '7.11.0', '7.10.2' ]

  stackVersions.edge()
  stackVersions.dev()
  stackVersions.release()
  stackVersions.snapshot(stackVersions.edge())
  stackVersions.edge(snapshot: true)

**/
def call(Map args = [:]) {
  return [
    edge(args),
    dev(args),
    release(args)
  ]
}

def snapshot(version){
  return "${version}-SNAPSHOT"
}

def version(value, args = [:]){
  return "${value}${isSnapshot(args)}"
}

def edge(Map args = [:]){
  return version("8.0.0", args)
}

def dev(Map args = [:]){
  return version("7.11.0", args)
}

def release(Map args = [:]){
  return version("7.10.2", args)
}

def isSnapshot(args){
  return args.containsKey('snapshot') && args.snapshot ? "-SNAPSHOT" : ''
}
