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

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue

class DummyStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/dummy.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
}

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(text: "dummy")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'I am a dummy step - dummy'))
    assertJobStatusSuccess()
  }
}

class DummyStepTestsWithMockInjection extends ApmBasePipelineTest {
  String scriptName = 'vars/dummy.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    /* This is an illustration of how to remove a default mock object. This would apply
    if the var in question (in this case, dummy.groovy was using `sh` which was then invoked
    by a test.

    To do this, ensure that the following conditions are met:

    1.  The mock object you wish to override is in the approved list
        entitled `allowedMockOverrides` which is present in the ApmBasePipelineTest
        class.
    2.  Make sure that getScriptedMethods contains the correct default for the
        mocked method. This should come from the registration method (ex: registerScriptedMethods())
    3.  Remove the default definition from the registration method (ex: registerScriptedMethods())
    4.  Create an injector as illustrated below with the key being the mock you wish to override and
        the value being a closure which defines its behavior.
    5.  Have fun storming the castle.
    */
    binding.setVariable('mockInjector', ['sh': {k ->
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(k);

        try {

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }])

    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(text: "dummy")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'I am a dummy step - dummy'))
    assertJobStatusSuccess()
  }
}
