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

package co.elastic

import hudson.model.Result
import javax.annotation.Nonnull
import jenkins.model.CauseOfInterruption

public class ExtendedFlowInterruptedException extends InterruptedException {

  private final String projectName
  private final int number

  private final @Nonnull Result result;
  private final @Nonnull List<CauseOfInterruption> causes;

  /**
    * Creates a new exception.
    * @param result the desired result for the flow, typically {@link Result#ABORTED}
    * @param causes any indications
    */
  public ExtendedFlowInterruptedException(@Nonnull Result result, @Nonnull CauseOfInterruption... causes) {
      this.result = result;
      this.causes = Arrays.asList(causes);
  }

  public ExtendedFlowInterruptedException(@Nonnull Result result, String projectName, int number, @Nonnull CauseOfInterruption... causes) {
    this(result, causes)
    this.projectName = projectName
    this.number = number
  }

  public @Nonnull Result getResult() {
    return result
  }

  public @Nonnull List<CauseOfInterruption> getCauses() {
    return causes
  }

  public String getProjectName() {
    return projectName
  }

  public int getNumber() {
    return number
  }
}
