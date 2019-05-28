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

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class BuildDocsStepTests extends BasePipelineTest {
  Map env = [:]

  def withEnvInterceptor = { list, closure ->
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

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("tar", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("writeFile", [Map.class], { "OK" })
    helper.registerAllowedMethod("checkoutElasticDocsTools", [Map.class], { "OK" })
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], withEnvInterceptor)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/buildDocs.groovy")
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/buildDocs.groovy")
    script.call(docsDir: "src", archive: true)
    printCallStack()
    assertJobStatusSuccess()
  }
}
