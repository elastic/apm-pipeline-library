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
    job: ['apm-agent-java', 'apm-agent-java-mbp', 'master']),
    return_boolean: false
**/

import java.net.URL
import java.net.HttpURLConnection

import java.io.BufferedReader
import java.io.InputStreamReader


private static String makeRequest(URL url) throws IOException {
    // URL url = new URL("${baseUrl}/${path}")
    HttpURLConnection con = (HttpURLConnection)url.openConnection()
    con.setRequestMethod("GET")
    con.setRequestProperty("User-Agent", "Jenkins Build Status/1.0")
    int responseCode = con.getResponseCode()

    InputStreamReader isr = new InputStreamReader(con.getInputStream())
    BufferedReader brd = new BufferedReader(isr)
    if (responseCode == HttpURLConnection.HTTP_OK) {
        String inputLine
        StringBuffer response = new StringBuffer()
        while ((inputLine = brd.readLine()) != null) {
            response.append(inputLine)
        }
        brd.close()
        return response.toString()
    } else {
        raise IOException("Failure to connect to Jenkins instance")
    }
}

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

def call(Map params = [:]) {
    def host = params.get('host', 'localhost')
    def job = params.get('job', [])
    def return_boolean = params.get('return_boolean', false)
    def ssl = params.get('ssl', true)
    def result = makeRequest(constructURL(host, job, ssl))
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
