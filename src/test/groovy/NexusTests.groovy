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
import org.junit.After
import org.junit.Test
import static org.junit.Assert.assertTrue
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer

class NexusTests extends ApmBasePipelineTest {
  String scriptName = 'src/co/elastic/Nexus.groovy'

  // Build a small test server
  def i = new InetSocketAddress('localhost', 9999)
  def HttpServer ws =  HttpServer.create(i, 100)

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    ws.start()
  }

  @After
  void tearDown() throws Exception {
    ws.stop(0)
  }

  @Test
  void testAcceptProperty() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.createConnection(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "dummy_path"
      )
      assertTrue(ret.getRequestProperty("Accept") == 'application/json')
  }

  @Test
  void testURL() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.createConnection(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "dummy_path"
      )
      assertTrue(ret.URL.toString().equals("http://localhost:9999/dummy_path"))
  }

  @Test
  void testAddData() throws Exception {
    def script = loadScript(scriptName)
    def conn = script.createConnection(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "dummy_path"
      )
    byte b = 100
    script.addData(conn, 'POST', b)
    assertTrue(conn.getRequestMethod().equals('POST'))
  }


}
