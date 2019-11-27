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

class HttpRequestStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/httpRequest.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    def body = script.call(url: "https://www.google.com", debug: 'true')
    printCallStack()
    assertTrue(body != null)
    assertJobStatusSuccess()
  }

  @Test
  void testGetWithParams() throws Exception {
    def script = loadScript(scriptName)
    def body = script.call(url: "https://www.google.com",
      method: "GET", headers: ["User-Agent": "dummy"], debug: 'true')
    printCallStack()
    assertTrue(body != null)
    assertJobStatusSuccess()
  }


  @Test
  void testPostWithParams() throws Exception {
    def script = loadScript(scriptName)
    def body = script.call(url: "https://duckduckgo.com",
      method: "POST",
      headers: ["User-Agent": "dummy"],
      data: "q=value&other=value")
    printCallStack()
    assertTrue(body != null)
    assertJobStatusSuccess()
  }

  @Test
  void testNoURL() throws Exception {
    def script = loadScript(scriptName)
    def message = ""
    try {
      script.call()
    } catch(e) {
      message = e.getMessage()
    }
    printCallStack()
    assertTrue(message.equals("httpRequest: Invalid URL"))
  }

  @Test
  void testInvalidURL() throws Exception {
    def script = loadScript(scriptName)
    def message = ""
    try {
      script.call(url: "htttttp://google.com")
    } catch(e) {
      message = e.getMessage()
    }
    printCallStack()
    assertTrue(message.equals("httpRequest: Invalid URL"))
  }

  @Test
  void testConnectionError() throws Exception {
    def script = loadScript(scriptName)
    def message = ""
    try {
      script.call(url: "https://thisdomaindoesnotexistforsure.com")
    } catch(e) {
      message = e.getMessage()
    }
    printCallStack()
    assertTrue(message.startsWith("httpRequest: Failure connecting to the service"))
  }

  @Test
  void testHttpError() throws Exception {
    def script = loadScript(scriptName)
    def message = ""
    try {
      script.call(url: "https://google.com",
        method: "POST",
        headers: ["User-Agent": "dummy"])
    } catch(e) {
      message = e.getMessage()
    }
    printCallStack()
    assertTrue(message.startsWith("httpRequest: Failure connecting to the service"))
  }
}
