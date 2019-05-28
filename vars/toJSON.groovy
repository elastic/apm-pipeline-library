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

import net.sf.json.JSONSerializer

/**
  This step converts a JSON string to net.sf.json.JSON or and POJO to net.sf.json.JSON.
  readJSON show the JSON in the Blue Ocean console output so it can not be used.
  https://issues.jenkins-ci.org/browse/JENKINS-54248

  net.sf.json.JSON obj = toJSON("{property: value, property1: value}")

  Person p = new Person();
  p.setName("John");
  p.setAge(50);
  net.sf.json.JSONObject obj = toJSON(p)
*/

def call(value){
  def obj = null
  if(value != null){
    try {
      obj = JSONSerializer.toJSON(value);
    } catch(e){
      //NOOP
      log(level: 'DEBUG', text: "toJSON: Unable to Parsing JSON: ${e?.message}" )
    }
  }
  return obj
}
