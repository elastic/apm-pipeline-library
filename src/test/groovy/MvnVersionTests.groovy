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
import static org.junit.Assert.assertEquals


class MvnVersionTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/mvnVersion.groovy')
    helper.registerAllowedMethod('sh', [Map.class], { return "1.1.82-SNAPSHOT" })
  }

  @Test
  void test() throws Exception {
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testCall() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', './mvnw help:evaluate -Dexpression=project.version -q -DforceStdout'))
  }

  @Test
  void testVersion() throws Exception {
    def ret = script.call()
    assertEquals("1.1.82-SNAPSHOT", ret)
  }

  @Test
  void testQualifiers() throws Exception {
    def ret = script.call(showQualifiers:false)
    assertEquals("1.1.82", ret)
  }

  @Test
  void testVersionMultiline() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { return """
Apache Maven 3.8.5 (3599d3414f046de2324203b78ddcf9b5e4388aa0)
Maven home: /Users/vmartinez/.m2/wrapper/dists/apache-maven-3.8.5-bin/5i5jha092a3i37g0paqnfr15e0/apache-maven-3.8.5
Java version: 17.0.5, vendor: Eclipse Adoptium, runtime: /Users/vmartinez/.sdkman/candidates/java/17.0.5-tem
Default locale: en_GB, platform encoding: UTF-8
OS name: "mac os x", version: "13.3", arch: "x86_64", family: "mac"
1.36.1-SNAPSHOT""" })
    def ret = script.call()
    assertEquals("1.36.1-SNAPSHOT", ret)
  }

}
