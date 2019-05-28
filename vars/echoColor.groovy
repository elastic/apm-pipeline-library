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

import groovy.transform.Field

/**
Print a text on color on a xterm.

echoColor(text: '[ERROR]', colorfg: 'red', colorbg: 'black')
*/

@Field Map colorsfgMap = [
'default': '39',
'black': '30',
'red': '31',
'green': '32',
'yellow': '33',
'blue': '34',
'magenta': '35',
'cyan': '36',
'light gray': '37',
'dark gray': '90',
'light red': '91',
'light green': '92',
'light yellow': '93',
'light blue': '94',
'light magenta': '95',
'light cyan': '96',
'white': '97'
]

@Field Map colorsbgMap = [
'default': '49',
'black': '40',
'red': '41',
'green': '42',
'yellow': '43',
'blue': '44',
'magenta': '45',
'cyan': '46',
'light gray': '47',
'dark gray': '100',
'light red': '101',
'light green': '102',
'light yellow': '103',
'light blue': '104',
'light magenta': '105',
'light cyan': '106',
'white': '107'
]

def call(Map params = [:]) {
  def text = params.containsKey('text') ? params.text : ''
  def colorfg = params.containsKey('colorfg') ? params.colorfg : 'default'
  def colorbg = params.containsKey('colorbg') ? params.colorbg : 'default'

  def colorfgValue = colorsfgMap[colorfg.toLowerCase()]
  def colorbgValue = colorsbgMap[colorbg.toLowerCase()]

  if(colorfgValue != null && colorbgValue != null){
    echo "\u001B[" + colorfgValue + ";" + colorbgValue + "m" + text + "\u001B[0m"
  } else {
    echo text
  }
}
