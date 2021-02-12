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

class StackVersionsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/stackVersions.groovy')
  }

  @Test
  void test() throws Exception {
    def versions = script.call()
    printCallStack()
    assertTrue(versions instanceof ArrayList)
    assertJobStatusSuccess()
  }

  @Test
  void testEdge() throws Exception {
    def versions = script.edge()
    printCallStack()
    assertTrue(versions != "")
    assertJobStatusSuccess()
  }

  @Test
  void testDev() throws Exception {
    def versions = script.dev()
    printCallStack()
    assertTrue(versions != "")
    assertJobStatusSuccess()
  }

  @Test
  void testRelease() throws Exception {
    def versions = script.release()
    printCallStack()
    assertTrue(versions != "")
    assertJobStatusSuccess()
  }

  @Test
  void testSnapshot() throws Exception {
    def versions = script.edge(snapshot: true)
    printCallStack()
    assertTrue(versions != "")
    assertTrue(versions.contains("-SNAPSHOT"))
    assertTrue(versions.contains(script.edge()))
    assertJobStatusSuccess()
  }
}
