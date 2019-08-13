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
    list.forEach {
      def fields = it.split("=")
      binding.setVariable(fields[0], fields[1])
    }
    def res = closure.call()
    list.forEach {
      def fields = it.split("=")
      binding.setVariable(fields[0], null)
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
