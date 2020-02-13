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
import org.junit.Rule
import org.junit.rules.ExpectedException
import java.io.File
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class NexusTests extends ApmBasePipelineTest {
  String scriptName = 'src/co/elastic/Nexus.groovy'

  def shInterceptor = {
    return """{
      "foo": "bar"
    }"""
  }
  File tmpFile = new File("/tmp/foo")

  @Rule
  public ExpectedException exception = ExpectedException.none();

  // Build a small test server
  def i = new InetSocketAddress('localhost', 9999)
  def HttpServer ws =  HttpServer.create(i, 100)
  HttpContext root_context = ws.createContext("/")
  HttpContext upload_context = ws.createContext("/up")

  @Override
  @Before
  void setUp() throws Exception {
    // System.println(this.handleRequest)
    tmpFile.write("Oh, hello there. This is a test file for NexusTests")
    super.setUp()
    root_context.setHandler({ exchange ->
      String response = shInterceptor();
      exchange.responseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(200, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
      exchange.Send
      });

    upload_context.setHandler({ exchange ->
      String response = shInterceptor();
      exchange.responseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(201, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
      exchange.Send
      });

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

  @Test
  void testGetData() throws Exception {
    helper.registerAllowedMethod("reader", [Map.class], shInterceptor)
    def script = loadScript(scriptName)
    def data
    def conn = script.createConnection(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "dummy_path"
      )

    data = script.getData(conn)

    printCallStack()
    // JSON-ify the stub sent by the webserver
    def toJson = loadScript('vars/toJSON.groovy')
    def expected_json = toJson(shInterceptor())
    assertTrue(expected_json.equals(data))
  }

  @Test
  void testcheckResponse() throws Exception {
    def script = loadScript(scriptName)
    def conn = script.createConnection(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "dummy_path"
      )
    script.checkResponse(conn, 200)

  }

  @Test
  void testcheckBadResponse() throws Exception {
    def script = loadScript(scriptName)
    def conn = script.createConnection(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "dummy_path"
      )
    exception.expect(Exception.class)
    script.checkResponse(conn, 900)
  }

  @Test
  void testUpload() throws Exception {
    def script = loadScript(scriptName)
    script.upload(
      "http://localhost:9999",
      "dummy_user",
      "dummy_pass",
      "up",
      tmpFile
      )
  }

  @Test
  void testGenerateHashFile() throws Exception {
    def filehandle = new File("/tmp/nexus_hash_file")
    filehandle.write("Oh, hello there. This is a test file for NexusTests")
    def algorithm = 'SHA'
    def script = loadScript(scriptName)
    def ret = script.generateHashFile(filehandle, algorithm)
    assertTrue(ret.toString().equals(filehandle.toString()+".SHA"))
    def hashFile = new File("/tmp/nexus_hash_file.SHA")
    assertTrue(hashFile.text.equals("e992a461191e8f12364bd1ecd2c8bec1be58cff8"))
  }

  @Test
  void testGetSnapshotsUrl() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.getSnapshotsURL('http://oss.sonatype.org')
    assertTrue(ret.equals("http://oss.sonatype.org/content/repositories/snapshots"))
  }

  @Test
  void testgetStagingURL() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.getStagingURL('http://oss.sonatype.org')
    assertTrue(ret.equals("http://oss.sonatype.org/service/local/staging"))
  }

  @Test
  void testGetStagingRepositoryURL() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.getStagingRepositoryURL('http://oss.sonatype.org', 'dummy_id')
    assertTrue(ret.equals("http://oss.sonatype.org/service/local/repositories/dummy_id/content"))
  }

  @Test
  void testIs5xxError() throws Exception {
    def script = loadScript(scriptName)
    assertTrue(script.is5xxError(500))
  }

  @Test
  void testIsNot5xxError() throws Exception {
    def script = loadScript(scriptName)
    assertFalse(script.is5xxError(404))
  }
}
