import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def params = parseUrlEncodedBody(context.request.body)
def grantType = params['grant_type']

if (grantType == 'urn:ietf:params:oauth:grant-type:pre-authorized_code') {
    def preAuthorizedCode = params['pre-authorized_code']
    def accessToken = buildAccessToken(preAuthorizedCode)
    def responseBody = [access_token: accessToken, token_type: "bearer", expires_in: 180]
    respond().withData(JsonOutput.toJson(responseBody))

} else if (grantType == 'authorization_code') {
    respond().withExampleName('authorization_code_openid')

} else if (grantType == 'urn:ietf:params:oauth:grant-type:token-exchange') {
    respond().withExampleName('token_exchange')

} else if (grantType == 'refresh_token') {
    respond().withExampleName('refresh_token')
}

/**
 * Parses a URL-encoded request body (application/x-www-form-urlencoded) into a map of key-value pairs.
 *
 * Splits the body on '&' delimiters and decodes each parameter name and value using UTF-8 encoding.
 * Parameters without values (no '=' present) are ignored.
 *
 * @param body The URL-encoded string to parse (e.g., "grant_type=authorization_code&code=abc123")
 * @return A map where keys are decoded parameter names and values are decoded parameter values
 */
static def parseUrlEncodedBody(String body) {
    def params = [:]
    body.split('&').each { param ->
        def parts = param.split('=', 2)
        if (parts.length == 2) {
            params[URLDecoder.decode(parts[0], 'UTF-8')] = URLDecoder.decode(parts[1], 'UTF-8')
        }
    }
    return params
}

/**
 * Builds an access token for the pre-authorized code flow.
 *
 * Decodes the pre-authorized code to extract the credential_identifiers claim, then builds an access token with:
 * - Header: ES256 algorithm, at+jwt type, and a fixed key ID
 * - Payload: subject, issuer, audience, credential identifiers from the pre-authorized code,
 *   a random c_nonce, 180-second expiration, and a random JWT ID
 * - Signature: hardcoded, so not cryptographically valid
 *
 * @param jwt The pre-authorized code
 * @return The access token
 */
def buildAccessToken(String jwt) {
    def preAuthCodeParts = jwt.split('\\.')

    def preAuthCodePayload = new String(Base64.decoder.decode(preAuthCodeParts[1]))
    def preAuthCodePayloadObject = new JsonSlurper().parseText(preAuthCodePayload)

    def accessTokenHeader = [alg: "ES256", typ: "at+jwt", kid: "C9De3xMDDyG7Nce4kGm09pCamzTMmYefPSmWw4FhnUg"]

    def selfUrl = System.getenv('SELF_URL')
    def issuerBaseUrl = System.getenv('ISSUER_URL')
    def accessTokenPayload = [
            sub                   : "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
            iss                   : selfUrl,
            aud                   : issuerBaseUrl,
            credential_identifiers: preAuthCodePayloadObject.credential_identifiers,
            c_nonce               : UUID.randomUUID().toString(),
            exp                   : (System.currentTimeMillis() / 1000).toLong() + 180,
            jti                   : UUID.randomUUID().toString()
    ]

    def encodedHeader = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(accessTokenHeader).bytes)
    def encodedPayload = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(accessTokenPayload).bytes)
    def signature = "yBpJ0zhIZWNQqpszXxbil8FmI0DcJ_JG7mHZlrBthVg16lkrcvj662Swl5tpXZbhm-k6LKsmh8CbiiCp-4bRkg"
    return "${encodedHeader}.${encodedPayload}.${signature}"
}
