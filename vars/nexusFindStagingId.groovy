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
  Find a  Nexus staging repository

  nexusFindStagingId(
    url: "https://oss.sonatype.org",
    secret: "secret/release/nexus"
    stagingProfileId: "comexampleapplication-1010",
    description: "My staging area"
    )
**/
import co.elastic.Nexus

def call(Map params = [:]) {

  String url = params.get('url', 'https://oss.sonatype.org')
  String stagingProfileId = params.containsKey('stagingProfileId') ? params.stagingProfileId : error('Must supply stagingProfileId')
  String description = params.containsKey('description') ? params.description : error('Must supply description')
  String secret = params.containsKey('secret') ? params.secret : 'secret/release/nexus'

  def props = getVaultSecret(secret: secret)

  if(props?.errors){
    error "Unable to get credentials from the vault: " + props.errors.toString()
  }

  def data = props?.data
  def username = data?.user
  def password = data?.password

  HttpURLConnection conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "profile_repositories/${stagingProfileId}")
  Nexus.checkResponse(conn, 200)
  Object response = Nexus.getData(conn)
  String repositoryId = null
  for (def repository : response['data']) {
      if (repository['description'] == description) {
          if (repository['type'] != 'open') {
              throw new Exception("Staging repository ${repository['repositoryId']} for '${description}' is not open. " +
                      "It should have been kept open when staging the release.")
          } else if (repositoryId != null) {
              throw new Exception("Multiple staging repositories exist for '${description}'. " +
                      "Please drop them manually from the sonatype website.")
          } else {
              repositoryId = repository['repositoryId']
          }
      }
  }
  if (repositoryId == null) {
      throw new Exception("Could not find staging repository for '${description}'")
  }
  return repositoryId
}
