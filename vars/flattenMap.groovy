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
  Convert the given map to be a flattened map.

  * map: map/dictionary to flatten
  * separator: character to use to separate map keys

  def map = ["a": ["b": ["c": 1]], ["d": 4]]
  def flattened = flattenMap(map: map, separator: '.')
*/
def call(Map args = [:]) {
  def map = args.containsKey('map') ? args.map: error('flattenMap: map parameter is required.')
  def separator = args.get('separator', '.')

  return flattenMap(map, separator)
}

def flattenMap(Map map, String separator) {
  return map.collectEntries { k, v ->
    v instanceof Map ?
      flattenMap(v, separator).collectEntries { q, r ->
        [(k + separator + q): r] } : [(k):v]
  }
}
