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
  Retry a command for a specified number of times until the command exits successfully.

  retryWithSleep(retries: 2) {
    //
  }

  // Retry up to 3 times with a 5 seconds wait period
  retryWithSleep(retries: 3, seconds: 5, exponential: true) {
    //
  }
*/
def call(Map args = [:], Closure body) {
  def i = args.containsKey('retries') ? args.retries : error('retryWithSleep: retries parameter is required.')
  def seconds = args.get('seconds', 10)
  def exponential = args.get('exponential', false)
  def sleepFirst = args.get('sleepFirst', false)

  def factor = 0
  retry(i) {
    factor++
    log(level: 'DEBUG', text: "retryWithSleep (${factor} of ${i} tries).")
    try {
      if(sleepFirst) { sleepWith(seconds, factor, exponential) }
      body()
    } catch(e) {
      if(!sleepFirst) { sleepWith(seconds, factor, exponential) }
      throw e
    }
  }
}

def sleepWith(seconds, factor, exponential) {
  def time = exponential ? (seconds * factor) : seconds
  log(level: 'DEBUG', text: "retryWithSleep. sleep ${time} seconds.")
  sleep(time)
}
