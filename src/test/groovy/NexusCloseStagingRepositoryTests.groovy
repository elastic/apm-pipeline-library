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

class nexusCloseStagingRepositoryTests extends ApmBasePipelineTest {
  String scriptName = 'vars/nexusCloseStagingRepository.groovy'

  def shInterceptor = {
    return """{
      "data": {"stagedRepositoryId": "pid"
    },
      "name": "close"
    }"""
  }

  def activityStub = """
  [{
  	"name": "close",
  	"events": [{
  		"name": "rulePassed",
  		"properties": [{
  			"value": "prop1"
  		}, {
  			"value": "prop2"
  		}]
  	}, {
  		"name": "repositoryClosed"
  	}]
  }]
  """

  // Build a small test server
  def i = new InetSocketAddress('localhost', 9999)
  def HttpServer ws =  HttpServer.create(i, 100)
  HttpContext root_context = ws.createContext("/service/local/staging/profiles/dummy_staging_profile_id/finish")
  HttpContext activity_context = ws.createContext("/service/local/staging/repository/dummy_staging_id/activity")


  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    root_context.setHandler({ exchange ->
      String response = shInterceptor();
      exchange.responseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(201, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
      exchange.Send
      });

    activity_context.setHandler({ exchange ->
      String response = activityStub;
      exchange.responseHeaders.set("Content-Type", "application/json")
      exchange.sendResponseHeaders(200, response.getBytes().length);
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
  void testClose() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call(
      url: 'http://localhost:9999',
      username: 'admin',
      password: 'pass',
      stagingProfileId: 'dummy_staging_profile_id',
      stagingId: 'dummy_staging_id')
  }
}
