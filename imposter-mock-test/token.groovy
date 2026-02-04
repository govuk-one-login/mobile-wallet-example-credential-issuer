import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.Signature
import java.security.KeyFactory
import java.security.spec.*
import java.security.AlgorithmParameters

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

// Determine which OAuth grant type is being requested
def grantType = params['grant_type']
def response

// Handle pre-authorized code flow (OID4VCI)
if (grantType == 'urn:ietf:params:oauth:grant-type:pre-authorized_code') {
    try {
        // Extract the pre-authorized code JWT from the request
        def jwt = params['pre-authorized_code']
        def jwtParts = jwt.split('\\.')

        // Decode the JWT header to get the key ID (`kid`) for signature verification
        def header = new JsonSlurper().parseText(new String(Base64.urlDecoder.decode(jwtParts[0])))
        def kid = header.kid

        // Fetch the issuer's public keys (JWKS) to verify the pre-authorized code signature
        def issuerBaseUrl = System.getenv('ISSUER_URL') ?: 'http://host.docker.internal:8080'
        def jwksUrl = "${issuerBaseUrl}/.well-known/jwks.json"
        def jwksText = new URL(jwksUrl).text
        def jwks = new JsonSlurper().parseText(jwksText)
        def jwk = jwks.keys.find { it.kid == kid }

        // Verify the pre-authorized code JWT signature using the issuer's public key
        verifyJWT(jwt, jwk)

        // Decode the pre-authorized code payload to extract the `credential_identifiers` claim
        def payload = new String(Base64.decoder.decode(jwtParts[1]))
        def payloadObj = new JsonSlurper().parseText(payload)
        
        // Build the access token response
        def selfUrl = System.getenv('SELF_URL') ?: 'http://host.docker.internal:9090'
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

        // Encode the JWT header and payload
        def encodedHeader = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(responseHeader).bytes)
        def encodedPayload = Base64.urlEncoder.withoutPadding().encodeToString(JsonOutput.toJson(jwtPayload).bytes)
        // This is a hardcoded signature for mocking purposes (not cryptographically valid)
        def signature = "yBpJ0zhIZWNQqpszXxbil8FmI0DcJ_JG7mHZlrBthVg16lkrcvj662Swl5tpXZbhm-k6LKsmh8CbiiCp-4bRkg"
        def token = "${encodedHeader}.${encodedPayload}.${signature}"

        // Return the token response
        response = [access_token: token, token_type: "bearer", expires_in: 180]
        respond().withData(JsonOutput.toJson(response))
    } catch (Exception exception) {
        println "Pre-authorized code processing failed: ${exception.message}"
        respond().withStatusCode(500)
        return
    }

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

// Verify JWT signature using ECDSA with SHA-256
// Validates that the JWT was signed by the holder of the private key corresponding to the provided public key
def verifyJWT(jwt, keyData) {
    def parts = jwt.split('\\.')
    def publicKey = buildECPublicKey(keyData)

    // Initialise the signature verifier with the public key
    def verifier = Signature.getInstance("SHA256withECDSA")
    verifier.initVerify(publicKey)
    verifier.update("${parts[0]}.${parts[1]}".bytes)  // Verify header.payload

    // Decode the signature and verify (converting from JWS to DER format)
    def sigBytes = Base64.urlDecoder.decode(parts[2])
    if (!verifier.verify(convertToDER(sigBytes))) {
        throw new Exception("JWT signature verification failed")
    }
}

// Construct an EC (Elliptic Curve) public key from JWK (JSON Web Key) format
// Converts the x and y coordinates from the JWK into a Java PublicKey object
def buildECPublicKey(keyData) {
    // Decode the x and y coordinates from base64url
    def x = new BigInteger(1, Base64.urlDecoder.decode(keyData.x))
    def y = new BigInteger(1, Base64.urlDecoder.decode(keyData.y))
    def ecPoint = new ECPoint(x, y)
    
    // Use the P-256 curve (secp256r1) parameters
    def params = AlgorithmParameters.getInstance("EC")
    params.init(new ECGenParameterSpec("secp256r1"))
    def ecParams = params.getParameterSpec(ECParameterSpec.class)
    
    // Generate the public key from the EC point and curve parameters
    def keySpec = new ECPublicKeySpec(ecPoint, ecParams)
    return KeyFactory.getInstance("EC").generatePublic(keySpec)
}

// Convert JWS ECDSA signature format (r||s concatenation) to DER format required by Java
// JWS uses raw r and s values concatenated, but Java's Signature.verify() expects DER encoding
def convertToDER(byte[] sig) {
    // Split the signature into r and s components (each half of the signature)
    int len = sig.length / 2
    byte[] r = new byte[len], s = new byte[len]
    System.arraycopy(sig, 0, r, 0, len)
    System.arraycopy(sig, len, s, 0, len)
    
    // Encode a single integer in DER format (ASN.1 INTEGER)
    def encode = { byte[] val ->
        // Remove leading zeros
        int start = 0
        while (start < val.length && val[start] == 0) start++
        if (start == val.length) return [0x02, 0x01, 0x00] as byte[]
        
        // Add a leading zero byte if the high bit is set (to keep the number positive)
        boolean needZero = (val[start] & 0x80) != 0
        int length = val.length - start + (needZero ? 1 : 0)
        byte[] result = new byte[2 + length]
        result[0] = 0x02  // INTEGER tag
        result[1] = (byte) length
        if (needZero) {
            result[2] = 0
            System.arraycopy(val, start, result, 3, length - 1)
        } else {
            System.arraycopy(val, start, result, 2, length)
        }
        return result
    }
    
    // Encode both r and s, then wrap in a SEQUENCE
    byte[] rDer = encode(r), sDer = encode(s)
    byte[] result = new byte[2 + rDer.length + sDer.length]
    result[0] = 0x30  // SEQUENCE tag
    result[1] = (byte)(rDer.length + sDer.length)
    System.arraycopy(rDer, 0, result, 2, rDer.length)
    System.arraycopy(sDer, 0, result, 2 + rDer.length, sDer.length)
    return result
}

