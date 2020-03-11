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

class BuildStatusTests extends ApmBasePipelineTest {
    String scriptName = 'vars/buildStatus.groovy'


    // Build a small test server
    def i = new InetSocketAddress('localhost', 9999)
    def HttpServer ws =  HttpServer.create(i, 100)
    HttpContext job_status_context = ws.createContext("/buildStatus/text")


    @Override
    @Before
    void setUp() throws Exception {
    super.setUp()

    job_status_context.setHandler({ exchange ->
    String response = "Success"
    exchange.responseHeaders.set("Content-Type", "text/plain;charset=utf-8")
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
    void test() throws Exception {
        def script = loadScript(scriptName)
        def result = script.call(host: 'localhost:9999', job: ['apm-agent-java', 'apm-agent-java-mbp', 'master'])
        assertTrue("Success" == result)
        assertJobStatusSuccess()
    }

    @Test
    void testBoolSuccess() throws Exception {
        def script = loadScript(scriptName)
        def result = script.call(host: 'localhost:9999', job: ['apm-agent-java', 'apm-agent-java-mbp', 'master'], return_boolean: true)
        assertTrue(result)
    }

}