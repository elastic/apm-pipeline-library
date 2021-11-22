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

package co.elastic

public class TestUtils {

  public static withEnvInterceptor = { list, closure ->
    def savedVars = []
    // filter invlaid entries - with name empty or containing only spaces
    list.findAll{ it&&it=~/^\s*[^=\s]+\s*=?/ }.forEach{
      List fields = it.split("=");
      def name = fields.remove(0);
      def value = fields.join("="); // add back = chars deleted by split
      savedVars.add([name, binding.hasVariable(name) ? binding.getVariable(name) : null])
      binding.setVariable(name, value.size()> 0 ? value : null)
    }
    def res = closure.call()
    savedVars.reverse().forEach {
      binding.setVariable(it[0], it[1])
    }
    return res
  }

  public static wrapInterceptor = { map, closure ->
    map.each { key, value ->
      if("varPasswordPairs".equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", "${it.password}")
        }
      }
    }
    def res = closure.call()
    map.forEach { key, value ->
      if("varPasswordPairs".equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", null)
        }
      }
    }
    return res
  }

  public static withEnvMaskInterceptor = { map, closure ->
    map.each { key, value ->
      if('vars'.equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", "${it.password}")
        }
      }
    }
    def res = closure.call()
    map.forEach { key, value ->
      if('vars'.equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", null)
        }
      }
    }
    return res
  }

  public static withCredentialsInterceptor = { list, closure ->
    list.forEach {
      env[it.variable] = "dummyValue"
    }
    def res = closure.call()
    list.forEach {
      env.remove(it.variable)
    }
    return res
  }
}
