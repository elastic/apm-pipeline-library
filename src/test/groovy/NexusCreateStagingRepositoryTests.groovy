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
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class NexusCreateStagingRepositoryTests extends ApmBasePipelineTest {

  def shInterceptor = {
    return """{
      "data": {"stagedRepositoryId": "pid"
    }
    }"""
  }

  // Build a small test server
  def i = new InetSocketAddress('localhost', 9999)
  def HttpServer ws =  HttpServer.create(i, 100)
  HttpContext root_context = ws.createContext("/")
  HttpContext profile_start_context = ws.createContext("/service/local/staging/profiles/pid/start")

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/nexusCreateStagingRepository.groovy')
    root_context.setHandler({ exchange ->
      String response = shInterceptor();
      exchange.responseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(200, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
      exchange.Send
      });

      profile_start_context.setHandler({ exchange ->
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
  void testCreate() throws Exception {
    def ret = script.call(
      url: 'http://localhost:9999',
      stagingProfileId: 'pid',
      secret: "secret/release/nexus",
      stagingId: 'foo',
      description: 'my desc')
    assertTrue(ret.equals('pid'))
  }
}

class NexusStagingCreate500Tests extends ApmBasePipelineTest {

  def shInterceptor = {
    return """{
      "data": {"stagedRepositoryId": "pid"
    }
    }"""
  }

  // Build a small test server
  def i = new InetSocketAddress('localhost', 9999)
  def HttpServer ws =  HttpServer.create(i, 100)
  HttpContext profile_start_context = ws.createContext("/service/local/staging/profiles/pid/start")

  @Override
  @Before
  void setUp() throws Exception {
    // System.println(this.handleRequest)
    super.setUp()
    script = loadScript('vars/nexusCreateStagingRepository')
      profile_start_context.setHandler({ exchange ->
        String response = shInterceptor();
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.sendResponseHeaders(500, response.getBytes().length);
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
  void testCreate500Err() throws Exception {
    /* This is a little funny because the code under test
    doesn't deal with a service never returning anything BUT a 500
    which is what this test does but we don't need to look that far
    ahead in this case */
    def ret
    try {
      ret = script.call(
        url: 'http://localhost:9999',
        stagingProfileId: 'pid',
        secret: "secret/release/nexus",
        description: 'my desc',
        retries: 0)
    } catch (e) {
      // NOOP
    }
    assertTrue(assertMethodCallContainsPattern("log", "Received a 500 HTTP response code while trying to create a staging repository in nexus, trying again."))
  }
}
