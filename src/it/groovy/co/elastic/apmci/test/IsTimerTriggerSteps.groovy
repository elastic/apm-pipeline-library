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

package co.elastic.apmci.test

// Add functions to register hooks and steps to this script.
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

// Define a world that represents the test environment.
// Hooks can set up and tear down the environment and steps
// can change its state, e.g. store values used by later steps.
class IsTimerTriggerStepWorld {

  String jobName
  String eventType

  void setJobName(String j) {
    jobName = j
  }

  void setEventType(String e) {
    eventType = e
  }

  boolean onlyCronTimerReturnsTrue() {
    if (eventType == "cron") {
      return true
    }

    return false
  }
}

// Create a fresh new world object as the test environment for each scenario.
// Hooks and steps will belong to this object so can access its properties
// and methods directly.
World {
  new IsTimerTriggerStepWorld()
}


Given(~'^the "([^"]*)" job is present$') { String jobName ->
  setJobName(jobName)
}

When(~'^the build is triggered by a "([^"]*)" event$') { String eventType ->
  setEventType(eventType)
}

Then(~'^the result of the build step is "([^"]*)"$') { boolean expected ->
  assert expected == onlyCronTimerReturnsTrue()
}
