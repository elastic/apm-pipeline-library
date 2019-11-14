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

package co.elastic.mock

import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * Mock Steps class.
 */
public class StepsMock implements Serializable {

  public RunWrapper build(Map params = [:]) {
    println "steps.build(job: '${params.job}')"
    return mockRunWrapper('SUCCESS')
  }

  private RunWrapper mockRunWrapper(String result) throws Exception {
    final RunWrapper runWrapper = mock(RunWrapper.class)
    when(runWrapper.getFullProjectName()).thenReturn("foo/bar")
    when(runWrapper.getNumber()).thenReturn(1)
    when(runWrapper.getDisplayName()).thenReturn("#1")
    when(runWrapper.getAbsoluteUrl()).thenReturn("http://example:8080/foo/job/bar")
    when(runWrapper.getCurrentResult()).thenReturn(result)
    return runWrapper
  }
}
