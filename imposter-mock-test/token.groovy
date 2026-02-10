import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// Parse the URL-encoded request body into a map of parameters
// This handles the OAuth token request body format (e.g., grant_type=...&pre-authorized_code=...)
def body = context.request.body
def params = [:]

body.split('&').each { param ->
    def parts = param.split('=', 2)
    if (parts.length == 2) {
        params[URLDecoder.decode(parts[0], 'UTF-8')] = URLDecoder.decode(parts[1], 'UTF-8')
    }
}

def grantType = params['grant_type']
def response

// Handle pre-authorized code flow (OID4VCI)
if (grantType == 'urn:ietf:params:oauth:grant-type:pre-authorized_code') {
    def jwt = params['pre-authorized_code']
    def jwtParts = jwt.split('\\.')

    // Decode the pre-authorized code payload to extract the `credential_identifiers` claim
    def payload = new String(Base64.decoder.decode(jwtParts[1]))
    def payloadObj = new JsonSlurper().parseText(payload)

    // Build the access token
    def selfUrl = System.getenv('SELF_URL') ?: 'http://host.docker.internal:9090'
    def issuerBaseUrl = System.getenv('ISSUER_URL') ?: 'http://host.docker.internal:8080'
    def responseHeader = [alg: "ES256", typ: "at+jwt", kid: "C9De3xMDDyG7Nce4kGm09pCamzTMmYefPSmWw4FhnUg"]
    def jwtPayload = [
            sub                   : "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
            iss                   : selfUrl,
            aud                   : issuerBaseUrl,
            credential_identifiers: payloadObj.credential_identifiers,
            c_nonce               : UUID.randomUUID().toString(),
            exp                   : (System.currentTimeMillis() / 1000).toLong() + 180,
            jti                   : UUID.randomUUID().toString()
    ]

    // Encode the access token header and payload
    def encodedHeader = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(responseHeader).bytes)
    def encodedPayload = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(jwtPayload).bytes)
    // This is a hardcoded signature for mocking purposes (not cryptographically valid)
    def signature = "yBpJ0zhIZWNQqpszXxbil8FmI0DcJ_JG7mHZlrBthVg16lkrcvj662Swl5tpXZbhm-k6LKsmh8CbiiCp-4bRkg"
    def token = "${encodedHeader}.${encodedPayload}.${signature}"

    // Return the token response
    response = [access_token: token, token_type: "bearer", expires_in: 180]
    respond().withData(JsonOutput.toJson(response))

// Handle authorization code flow - use OpenAPI spec example
} else if (grantType == 'authorization_code') {
    respond().withExampleName('authorization_code_openid')

// Handle token exchange flow - use OpenAPI spec example
} else if (grantType == 'urn:ietf:params:oauth:grant-type:token-exchange') {
    respond().withExampleName('token_exchange')

// Handle refresh token flow - use OpenAPI spec example
} else if (grantType == 'refresh_token') {
    respond().withExampleName('refresh_token')
}