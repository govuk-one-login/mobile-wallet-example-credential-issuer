import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.util.Base64

def body = context.request.body
def contentType = context.request.headers['Content-Type']

def params = [:]
if (contentType?.contains('application/json')) {
  params = new JsonSlurper().parseText(body)
} else {
  body.split('&').each { param ->
    def parts = param.split('=', 2)
    if (parts.length == 2) {
      params[URLDecoder.decode(parts[0], 'UTF-8')] = URLDecoder.decode(parts[1], 'UTF-8')
    }
  }
}

def grantType = params['grant_type']
def response

if (grantType == 'urn:ietf:params:oauth:grant-type:pre-authorized_code') {
  def issuer = System.getenv('MOCK_ISSUER') ?: 'https://default-issuer.com'
  def jwt = params['pre-authorized_code']
  def jwtParts = jwt.split('\\.')
  def payload = new String(Base64.decoder.decode(jwtParts[1]))
  def payloadObj = new JsonSlurper().parseText(payload)
  
  def header = [alg: "ES256", typ: "at+jwt", kid: "8f9ec544-f5df-4d37-a32d-a5defd78ab0f"]
  def jwtPayload = [
    sub: "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
    iss: issuer,
    aud: payloadObj.iss,
    credential_identifiers: payloadObj.credential_identifiers,
    c_nonce: UUID.randomUUID().toString(),
    exp: (System.currentTimeMillis() / 1000).toLong() + 3600,
    jti: UUID.randomUUID().toString()
  ]
  
  def encodedHeader = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(header).bytes)
  def encodedPayload = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(jwtPayload).bytes)
  def signature = "yBpJ0zhIZWNQqpszXxbil8FmI0DcJ_JG7mHZlrBthVg16lkrcvj662Swl5tpXZbhm-k6LKsmh8CbiiCp-4bRkg"
  def token = "${encodedHeader}.${encodedPayload}.${signature}"
  
  response = [access_token: token, token_type: "bearer", expires_in: 3600]
  
} else if (grantType == 'authorization_code') {
  response = [access_token: "mock_access_${UUID.randomUUID()}", token_type: "bearer", expires_in: 3600, refresh_token: "mock_refresh_${UUID.randomUUID()}"]
  
} else if (grantType == 'urn:ietf:params:oauth:grant-type:token-exchange') {
  response = [access_token: "mock_service_${UUID.randomUUID()}", issued_token_type: "urn:ietf:params:oauth:token-type:access_token", token_type: "bearer", expires_in: 180]
  
} else if (grantType == 'refresh_token') {
  response = [access_token: "mock_refreshed_${UUID.randomUUID()}", token_type: "bearer", expires_in: 3600, refresh_token: "mock_new_refresh_${UUID.randomUUID()}"]
  
} else {
  respond().withStatusCode(400).withHeader('Content-Type', 'application/json').withData(JsonOutput.toJson([error: "unsupported_grant_type", error_description: "Unsupported grant type"]))
  return
}

respond().withStatusCode(200).withHeader('Content-Type', 'application/json').withData(JsonOutput.toJson(response))
