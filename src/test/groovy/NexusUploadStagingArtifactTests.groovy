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

import groovy.mock.interceptor.MockFor
import co.elastic.Nexus


class NexusUploadStagingArtifactTests extends ApmBasePipelineTest {
  String scriptName = 'vars/nexusUploadStagingArtifact.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testUpload() throws Exception {

    def nexusMock = new MockFor(co.elastic.Nexus)

    nexusMock.demand.getStagingURL(1){ url -> 'http://foo.com' }
    nexusMock.demand.generateHashFile(2){fh, enc -> new File("/tmp/dummy")}
    nexusMock.demand.upload(3){ url, username, password, path, fh -> 200 }

    def script = loadScript(scriptName)

    nexusMock.use {
      def ret = script.call(
        url: 'http://localhost:9999',
        username: 100,
        password: 'dummy_password',
        stagingId: 'co.dummy.corp',
        artifactId: 'dummy_artifact',
        version: 'v1.0.0',
        file_path: '/tmp/foo',
        groupId: 'co.elastic.apm'
        )

      assertTrue(ret == 200)
    }
  }
}
