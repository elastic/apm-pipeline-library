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
Get the build status of a job from a local or remote Jenkins instanve

buildStatus(
    host: 'localhost',
    job: ['apm-agent-java', 'apm-agent-java-mbp', 'main']),
    return_boolean: false
**/

import java.net.URL


private static URL constructURL(String host, ArrayList job, boolean ssl) throws Exception {
    String delim = "%2F"
    String job_path = job.join(delim)
    String uri
    if (ssl){
      uri = "https://${host}/buildStatus/text?job=${job_path}"
    } else {
      uri = "http://${host}/buildStatus/text?job=${job_path}"
    }
    URL url = new URL(uri)
    return url
}

def call(Map args = [:]) {
    def host = args.get('host', 'localhost')
    def job = args.get('job', [])
    def return_boolean = args.get('return_boolean', false)
    def ssl = args.get('ssl', true)
    def to_url = constructURL(host, job, ssl).toString()
    def result = httpRequest(url: to_url)
    if (return_boolean){
        if (result == "Success") {
            return true
        } else {
            return false
        }
    } else {
        return result
    }
}
