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
 Return the name of the Operating system based on the labels of the Node.

 def os = nodeOS()

*/
def call() {
  def labels = env.NODE_LABELS?.toLowerCase()
  def matches = []

  if (isLinux(labels) || isArm()) {
    matches.add('linux')
  }

  if (isWindows(labels)) {
    matches.add('windows')
  }

  if (isDarwin(labels)) {
    matches.add('darwin')
  }

  if(matches.size() == 0){
    error("Unhandled OS name in NODE_LABELS: " + labels)
  }

  if(matches.size() > 1){
    error("Labels conflit OS name in NODE_LABELS: " + labels)
  }

  return matches[0]
}

def isLinux(labels){
  return labels.contains('linux')
}

def isDarwin(labels){
  return labels.contains('darwin') || labels.contains('macos')
}

def isWindows(labels){
  return labels.contains('windows')
}
