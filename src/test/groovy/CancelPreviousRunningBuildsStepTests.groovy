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

import co.elastic.mock.RunMock
import co.elastic.mock.RunWrapperMock
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

public class CancelPreviousRunningBuildsStepTests extends ApmBasePipelineTest {
  def script

  def runBuilding = new RunMock(building: true)
  def runNotBuilding = new RunMock(building: false)
  def build1 = new RunWrapperMock(rawBuild: runNotBuilding, number: 1)
  def build2 = new RunWrapperMock(previousBuild: build1, rawBuild: runBuilding, number: 2)
  def build3 = new RunWrapperMock(previousBuild: build2, rawBuild: runBuilding, number: 3)
  def build4 = new RunWrapperMock(previousBuild: build3, rawBuild: runBuilding, number: 4)
  def build5 = new RunWrapperMock(previousBuild: build4, rawBuild: runNotBuilding, number: 5)
  def build6 = new RunWrapperMock(previousBuild: build5, rawBuild: runBuilding, number: 6)

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/cancelPreviousRunningBuilds.groovy')
    binding.setVariable('currentBuild', build2)
  }

  @Test
  void testDefault() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', '10'))
  }

  @Test
  void testWithZeroBuilds() throws Exception {
    binding.setVariable('currentBuild', build6)
    script.call(maxBuildsToSearch: 0)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', '0'))
    assertFalse(assertMethodCallContainsPattern('log', 'stop'))
  }

  @Test
  void testDefaultWithMoreBuilds() throws Exception {
    binding.setVariable('currentBuild', build6)
    script.call()
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'build #5'))
    assertTrue(assertMethodCallContainsPattern('log', 'build #4'))
    assertTrue(assertMethodCallContainsPattern('log', 'build #3'))
    assertTrue(assertMethodCallContainsPattern('log', 'build #2'))
  }

  @Test
  void testWith2MaxBuildsToSearch() throws Exception {
    binding.setVariable('currentBuild', build6)
    script.call(maxBuildsToSearch: 2)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', '2'))
    assertFalse(assertMethodCallContainsPattern('log', 'build #5'))
    assertTrue(assertMethodCallContainsPattern('log', 'build #4'))
    assertFalse(assertMethodCallContainsPattern('log', 'build #3'))
    assertFalse(assertMethodCallContainsPattern('log', 'build #2'))
  }
}
