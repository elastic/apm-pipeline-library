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

@Grab(group='io.jsonwebtoken', module='jjwt-impl', version='0.11.2')

import io.jsonwebtoken.Jwts
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.KeyFactory
import java.util.Date
import org.apache.commons.codec.binary.Base64
import static io.jsonwebtoken.SignatureAlgorithm.RS256

/**
  Send GitHub check step

  githubCheck(name: 'checkName', description: 'Execute something')

*/
def call(Map args = [:]) {
  def name = args.containsKey('name') ? args.name : error('githubCheck: Missing arguments')
  def description = args.get('description', name)
  def body = args.get('body', '')
  def secret = args.get('secret', 'secret/observability-team/ci/github-app-token')
  def org = args.get('org', env.ORG_NAME)
  def repository = args.get('repository', env.REPO_NAME)
  def commitId = args.get('commitId', env.GIT_BASE_COMMIT)
  def status = args.get('status', 'neutral')
  def detailsUrl = args.get('detailsUrl', '')

  // Read secrets from vault
  def props = getVaultSecret(secret: secret)
  if(props?.errors){
    error "Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def privateKeyContent = props?.data?.privateKeyContent
  def installationId = props?.data?.installationId
  def appId = props?.data?.appId
  // End read secrets from vault

  // App login details
  // TODO: potentially to be cached if within the valid timeframe.
  def jsonWebToken = getJsonWebToken(privateKeyContent: privateKeyContent, appId: appId)
  def token = getToken(jsonWebToken: jsonWebToken, installationId: installationId)
  def parameters = [
                    checkName: name,
                    commitId: commitId,
                    org: org,
                    repository: repository,
                    status: status,
                    token: token
                  ]
  def checkRunId = getPreviousCheckNameRunIdIfExists(parameters)
  parameters << [
    output: [
      title: name,
      summary: description,
      text: body
    ],
    checkRunId: checkRunId,
    detailsUrl: detailsUrl
  ]
  if (checkRunId) {
    updateCheck(parameters)
  } else {
    createCheck(parameters)
  }
}

def getPreviousCheckNameRunIdIfExists(Map args = [:]) {
  try {
    def checkRuns = githubApiCall(token: args.token,
                                  url: "https://api.github.com/repos/${args.org}/${args.repository}/commits/${args.commitId}/check-runs",
                                  headers: ['Accept': 'application/vnd.github.v3+json'])
    return checkRuns?.check_runs?.find { it.name == args.checkName }?.id
  } catch(Exception e){
    return false
  }
}

def createCheck(Map args = [:]) {
  args.method = 'POST'
  setCheckName(args)
}

def updateCheck(Map args = [:]) {
  args.method = 'PATCH'
  setCheckName(args)
}

def setCheckName(Map args = [:]) {
  def token = args.token
  def org = args.org
  def repository = args.repository
  def checkName = args.checkName
  def status = args.get('status', 'neutral')
  def method = args.get('method', 'POST')
  def commitId = args.get('commitId', null)
  def checkRunId = args.get('checkRunId', null)
  def output = args.get('output', [:])
  def detailsUrl = args.get('detailsUrl', '')

  try {
    def when = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    // See https://docs.github.com/en/free-pro-team@latest/rest/reference/checks#create-a-check-run--parameters
    def data = [ 'name': "${checkName}",
                 'status': "in_progress",
                 'conclusion': "${status}",
                 'completed_at': "${when}",
                 'output': output,
                 'details_url': detailsUrl
               ]
    def url = "https://api.github.com/repos/${org}/${repository}/check-runs"

    if (method == 'POST') {
      data['head_sha'] = "${commitId}"
    } else {
      url += "/${checkRunId}"
    }

    return githubApiCall(token: token,
                         url: url,
                         headers: ['Accept': 'application/vnd.github.v3+json'],
                         method: method,
                         data: data,
                         noCache: true)
  } catch(Exception e){
    log(level: 'ERROR', text: "Exception: ${e}")
    error 'setCheckName: Failed to create a check run'
  }
}

def getJsonWebToken(Map args=[:]) {
  try {
    return Jwts.builder()
            .setSubject('RS256')
            .signWith(RS256, getRSAPrivateKey(args.privateKeyContent))
            .setExpiration(new Date((new Date()).getTime() + 50000l))
            .setIssuedAt(new Date(System.currentTimeMillis() + 1000))
            .setIssuer(args.appId)
            .compact()
  } catch(Exception e){
    log(level: 'ERROR', text: "Exception: ${e}")
    error 'getJsonWebToken: Failed to create a JWT'
  }
}

def getRSAPrivateKey(privateKeyPEM) {
  try {
    // Expected the discarded bits to be zero.
    privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
    privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "")

    byte[] encoded = Base64.decodeBase64(privateKeyPEM)
    KeyFactory kf = KeyFactory.getInstance("RSA")
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded)
    RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec)
    return privateKey
  } catch(Exception e){
    log(level: 'ERROR', text: "Exception: ${e}")
    error 'getRSAPrivateKey: Failed to create a JWT'
  }
}

def getToken(Map args=[:]) {
  try {
    return githubApiCall(authorizationType: 'Bearer',
                         token: args.jsonWebToken,
                         url: "https://api.github.com/app/installations/${args.installationId}/access_tokens",
                         headers: ['Accept': 'application/vnd.github.v3+json'],
                         forceMethod: true,
                         noCache: true)?.token
  } catch(Exception e){
    log(level: 'ERROR', text: "Exception: ${e}")
    error 'getToken: Failed to create a JWT'
  }
}


  
