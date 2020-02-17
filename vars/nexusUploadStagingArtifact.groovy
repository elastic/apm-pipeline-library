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
  Upload an artifact to the Nexus staging repository

  nexusUploadStagingArtifact(
    url: "https://oss.sonatype.org",
    secret: "secret/release/nexus",
    stagingId: "comexampleapplication-1010",
    groupId: "com.example.applications",
    artifactId: "my_tasty_artifact",
    version: "v1.0.0"
    file_path: "/tmp/my_local_artifact"
    )
**/
import co.elastic.Nexus

def call(Map params = [:]){
  String url = params.get('url', 'https://oss.sonatype.org')
  String secret = params.containsKey('secret') ? params.secret : 'secret/release/nexus'
  String stagingId = params.containsKey('stagingId') ? params.stagingId : error('Must supply stagingId')
  String groupId = params.containsKey('groupId') ? params.groupId : error('Must supply groupId')
  String artifactId = params.containsKey('artifactId') ? params.groupId : error('Must supply artifactId')
  String version = params.containsKey('version') ? params.version : error('Must supply version')
  String file_path = params.containsKey('file_path') ? params.file_path : error('Must supply file_path')

  def props = getVaultSecret(secret: secret)
  
  if(props?.errors){
     error "Unable to get credentials from the vault: " + props.errors.toString()
  }

  def vault_data = props?.data
  def username = vault_data?.user
  def password = vault_data?.password

  String stagingURL = Nexus.getStagingURL(url)
  log(level: "INFO", text: "Load artifact for staging from " + file_path)
  File fh = new File(file_path)
  log(level: "INFO", text: "Staging " + artifactId)

  String path = "deployByRepositoryId/${stagingId}/${groupId.replace('.', '/')}/${artifactId}/${version}"

  File md5_f = Nexus.generateHashFile(fh, 'md5')
  File sha1_f = Nexus.generateHashFile(fh, 'sha1')
  withEnvMask(vars: [
    [var: "NEXUS_username", password: username],
    [var: "NEXUS_password", password: password]    ]){
      Nexus.upload(stagingURL, env.NEXUS_username, env.NEXUS_password, path, fh)
    }

  // The *.asc upload has been disabled because it doesn't seem necessary but there is a chance
  // that oss.sonatype.org will require it so I am keeping it here for the time being just in case
  // it is needed once we deploy to production.
  // Nexus.upload(stagingURL, username, password, path, new File(fh.path + '.asc'))
  //
  // // hack for sonatype since they require sha1 and md5 hashes as well, we generate the extra sha1 and md5 files on the fly here
  Nexus.upload(stagingURL, username, password, path, md5_f)
  Nexus.upload(stagingURL, username, password, path, sha1_f)
}
