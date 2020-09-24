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
 Return the architecture in the current worker using the labels as the
 source of truth

 def arch = nodeArch()

*/
def call() {
  def labels = env.NODE_LABELS?.toLowerCase()
  def matches = []

  if (is32bit(labels)) {
    matches.add('i386')
  }

  if (is64bit(labels)) {
    matches.add('x86_64')
  }

  if (isArm64(labels)) {
    matches.add('aarch64')
  }

  if (isArm(labels) && !isArm64(labels)) {
    matches.add('arm')
  }

  if(matches.size() == 0){
    error("Unhandled arch in NODE_LABELS: ${labels}")
  }

  if(matches.size() > 1){
    println matches
    error("Labels conflict arch in NODE_LABELS: ${labels}")
  }

  return matches[0]
}

def is32bit(labels){
  return labels.contains('i386')
}

def is64bit(labels){
  return labels.contains('x86_64')
}

def isArm(labels){
  return labels?.split(' ').find{ it.equals('arm')} ? true : false
}

def isArm64(labels){
  return labels.contains('aarch64')
}
