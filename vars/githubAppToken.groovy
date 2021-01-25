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
  Get the GitHub APP token

  def token = githubAppToken()

*/
def call(Map args = [:]) {
  // Read secrets from vault
  def props = getVaultSecret(secret: args.get('secret', 'secret/observability-team/ci/github-app'))
  if(props?.errors){
    error "Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def privateKeyContent = props?.data?.get('key')
  def installationId = props?.data?.get('installation_id')
  def appId = props?.data?.get('app_id')
  // End read secrets from vault

  // App login details
  // TODO: potentially to be cached if within the valid timeframe.
  return getToken(jsonWebToken: getJsonWebToken(privateKeyContent: privateKeyContent, appId: appId),
                  installationId: installationId)
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
