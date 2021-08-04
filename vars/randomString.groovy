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
Generate a random string

  // Create a random string of 15 chars (alphanumeric)
  def value = randomString(size: 15)
*/

import java.security.SecureRandom
import java.util.Base64

def call(Map args = [:]) {
  def size = args.get('size', 128)
  // Generate a random byte size bigger than the random string size.
  def byteSize = size * 5
  SecureRandom rand = new SecureRandom()
  byte[] randomBytes = new byte[byteSize]
  rand.nextBytes(randomBytes)

  def value = manipulateString(new String(Base64.encoder.encode(randomBytes)), size)
  return value
}

/**
  Alphanumeric and dash are allowed but not ending nor starting with dash
*/
def manipulateString(String value, int size) {
  return value.replaceAll("[\\W]|-", "-").take(size).replaceAll('-$', 'a').replaceAll('^-', 'a')
}
