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

import co.elastic.mock.OtelHelperMock
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class WithOtelEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withOtelEnv.groovy')
    addEnvVar('TRACE_ID', '12345678')
    addEnvVar('SPAN_ID', 'abcdefg')
  }

  @Test
  void test_plugin_not_installed() throws Exception {
    binding.setProperty('otelHelper', new OtelHelperMock(false, false))
    try {
      script.call(){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withOtelEnv: opentelemetry plugin is not available'))
    assertJobStatusFailure()
  }

  @Test
  void test() throws Exception {
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=otel-token, variable=OTEL_TOKEN_ID'))
    // This particular dummyValue is generated in src/test/groovy/co/elastic/TestUtils.groovy#withCredentialsInterceptor
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'ELASTIC_APM_SECRET_TOKEN, password=dummyValue'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'ELASTIC_APM_SERVER_URL, password=otel-endpoint'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'OTEL_EXPORTER_OTLP_ENDPOINT, password=otel-endpoint'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'OTEL_EXPORTER_OTLP_HEADERS, password=authorization=Bearer dummyValue'))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'TRACEPARENT=00-12345678-abcdefg-01'))
  }

  @Test
  void test_when_OTEL_EXPORTER_OTLP_HEADERS_defined() throws Exception {
    addEnvVar('OTEL_EXPORTER_OTLP_HEADERS', 'foo=bar')
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'OTEL_EXPORTER_OTLP_HEADERS, password=foo=bar authorization=Bearer dummyValue'))
  }

  @Test
  void test_when_TRACEPARENT_defined() throws Exception {
    addEnvVar('TRACEPARENT', '00-foo-bar-01')
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertFalse(assertMethodCallContainsPattern('withEnv', 'TRACEPARENT'))
  }

  @Test
  void test_when_credentialsId() throws Exception {
    def isOK = false
    script.call(credentialsId: 'my-foo-id'){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=my-foo-id, variable=OTEL_TOKEN_ID'))
  }

  @Test
  void test_plugin_not_configured() throws Exception {
    binding.setProperty('otelHelper', new OtelHelperMock(true, false))
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
  }

}
