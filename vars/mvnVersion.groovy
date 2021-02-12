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

/**
Get a project version from Maven

mvnVersion(
    showQualifiers: true)
**/

def call(Map args = [:]) {
    def showQualifiers = args.get('showQualifiers', true)
    def ver
    try {
        ver = sh(script: "./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true)
    } catch (err) {
        error "Error executing Maven. Check to ensure that you are in the project root and that `mvnw` and `pom.xml` are present."
        throw err
    }
    if (!showQualifiers){
        return ver.replaceAll(/-.*$/, '')
    } else {
        return ver
    }
}
