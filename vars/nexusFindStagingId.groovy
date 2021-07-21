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
    stagingProfileId: "1234-1455-1242",
    groupId: co.elastic.apm
    username: nexus
    password: my_password
    )
**/
import co.elastic.Nexus

def call(Map args = [:]) {
  String url = args.get('url', 'https://oss.sonatype.org')
  String username = args.get('username')
  String password = args.get('password')
  String stagingProfileId = args.containsKey('stagingProfileId') ? args.stagingProfileId : error('Must supply stagingProfileId')
  String groupId = args.containsKey('groupId') ? args.groupId : error('Must supply group id')
  String role_id = args.get('role_id')
  String secret_id = args.get('secret_id')

  def HttpURLConnection conn
  String stagingURL = Nexus.getStagingURL(url)
  conn = Nexus.createConnection(stagingURL, username, password, "profile_repositories/${stagingProfileId}")
  Nexus.checkResponse(conn, 200)
  Object response = Nexus.getData(conn)
  String repositoryId = null
  String mungeGroupId = groupId.replace(".", "")

  def props = getVaultSecret(secret: secret, role_id, secret_id)

  if(props?.errors){
    error "Unable to get credentials from the vault: " + props.errors.toString()
  }

  def data = props?.data
  def username = data?.username
  def password = data?.password


  for (def repository : response['data']) {
      // We can't look for the description if we didn't actually open the staging repo
      // because they are automatically generated.
      // https://central.sonatype.org/pages/releasing-the-deployment.html
      // This is a workaround to just look for open repos that begin with our groupId
      if (repository['repositoryId'].startsWith(mungeGroupId)) {
          if (repository['type'] != 'open') {
               error "Staging repository ${repository['repositoryId']} for '${groupId}' is not open. " +
                      "It should have been kept open when staging the release."
          } else if (repositoryId != null) {
              error "Multiple staging repositories exist for '${groupId}'. " +
                      "Please drop them manually from the sonatype website."
          } else {
              repositoryId = repository['repositoryId']
          }
      }
  }
  if (repositoryId == null) {
      error "Could not find staging repository for '${groupId}'"
  }
  return repositoryId
}
