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
import java.util.Arrays
import jenkins.model.CauseOfInterruption

public class BuildException extends InterruptedException {
    final String number
    final Result result
    final List<CauseOfInterruption> causes
    private Boolean actualInterruption = true

    public BuildException(String number) {
        this.number = number
    }

    public BuildException(String number, Result result, CauseOfInterruption... causes) {
        this.number = number
        this.result = result
        this.causes = Arrays.asList(causes)
        this.actualInterruption = true
    }

    public BuildException(String number, Result result, boolean actualInterruption, CauseOfInterruption... causes) {
        this.number = number
        this.result = result
        this.causes = Arrays.asList(causes)
        this.actualInterruption = actualInterruption
    }

    public String getNumber() {
        return number
    }

    public Result getResult() {
        return result
    }

    public List<CauseOfInterruption> getCauses() {
        return causes
    }

    public boolean isActualInterruption() {
        return actualInterruption
    }
}
