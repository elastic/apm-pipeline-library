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
Allow to print messages with different levels of verbosity. It will show all messages that match
to an upper log level than defined, the default level is debug.
You have to define the environment variable PIPELINE_LOG_LEVEL to select
the log level by default is INFO.

 Levels: DEBUG, INFO, WARN, ERROR

 log(level: 'INFO', text: 'message')

*/
def call(Map args = [:]) {
  def level = args.containsKey('level') ? getLogLevelNum(args.level) : getLogLevelNum('DEBUG')
  def text = args.containsKey('text') ? args.text : ''
  def currentLevel = getLogLevelNum(env?.PIPELINE_LOG_LEVEL)
  if( level >= currentLevel){
    logMessage(level, text)
  }
}

def logMessage(level, text){
  switch(level) {
    case 0:
      echoColor(text: "[DEBUG] ${text}", colorfg: 'blue', colorbg: 'default')
    break
    case 1:
      echoColor(text: "[INFO] ${text}", colorfg: 'default', colorbg: 'default')
    break
    case 2:
      echoColor(text: "[WARN] ${text}", colorfg: 'yellow', colorbg: 'default')
    break
    case 3:
      echoColor(text: "[ERROR] ${text}", colorfg: 'red', colorbg: 'default')
    break
    default:
      echoColor(text: "[DEBUG] ${text}", colorfg: 'blue', colorbg: 'default')
    break
  }
}

def getLogLevelNum(level){
  def levelNum = 1
  switch(level) {
    case 'DEBUG':
      levelNum = 0
    break
    case 'INFO':
      levelNum = 1
    break
    case 'WARN':
      levelNum = 2
    break
    case 'ERROR':
      levelNum = 3
    break
    default:
      levelNum = 1
    break
  }
  return levelNum
}
