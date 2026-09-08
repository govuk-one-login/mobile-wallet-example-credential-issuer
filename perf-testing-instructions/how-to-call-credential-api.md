# How to Call the /credential API – Functional Steps

This document describes the functional steps performed by the test harness (`test-harness/test/credential-issuer-tests.test.ts`) to construct and call the `/credential` API. Written as a walkthrough for pentesters.

---

## Overview

The test simulates the GOV.UK Wallet's behaviour: it receives a credential offer, extracts auth material from it, constructs a self-signed access token and a proof-of-possession JWT, then POSTs to the issuer's `/credential` endpoint.

---

## Step 1: Parse the Credential Offer Deep Link

The test starts with a credential offer deep link (a URL). It:

1. Extracts the `credential_offer` query parameter from the deep link URL.
2. JSON-parses the value into a credential offer object with this shape:
   ```json
   {
     "credential_issuer": "https://issuer.example.com",
     "credential_configuration_ids": ["some-credential-config-id"],
     "grants": {
       "urn:ietf:params:oauth:grant-type:pre-authorized_code": {
         "pre-authorized_code": "<JWT>"
       }
     }
   }
   ```
3. Extracts the `pre-authorized_code` (a JWT) and the `credential_configuration_ids`.

---

## Step 2: Decode the Pre-Authorized Code

The `pre-authorized_code` is a JWT. The test decodes it (without verifying its signature at this stage) to extract claims. Key claims used later:

- `iss` — the credential issuer's identifier (used as `audience` in tokens the wallet creates)
- `aud` — the expected audience (the auth server / wallet URL, used as `issuer` in the access token)
- `credential_configuration_ids` — which credential types are available
- `credential_identifiers` — specific credential identifiers to request

---

## Step 3: Discover the Credential Endpoint

The test fetches the issuer's OpenID credential issuer metadata from:

```
GET {CRI_URL}/.well-known/openid-credential-issuer
```

From the response it extracts:
- `credential_endpoint` — the URL to POST credential requests to
- `notification_endpoint` — (optional) for lifecycle notifications
- `mdoc_iacas_uri` — (optional) for mDL root certificates

---

## Step 4: Generate a Nonce

A random UUID is generated to use as the `c_nonce` (credential nonce). This nonce ties the access token to the proof JWT — the issuer verifies they match.

---

## Step 5: Construct the Access Token

The test creates a self-signed JWT access token with the following structure:

**Header:**
```json
{
  "alg": "ES256",
  "typ": "at+jwt",
  "kid": "<configured key ID>"
}
```

**Payload:**
```json
{
  "sub": "<wallet_subject_id>",
  "iss": "<pre-authorized_code.aud>",
  "aud": "<pre-authorized_code.iss>",
  "jti": "<random UUID>",
  "exp": "<now + 180 seconds>",
  "credential_configuration_ids": ["<from pre-authorized code>"],
  "credential_identifiers": ["<from pre-authorized code>"],
  "c_nonce": "<the generated nonce from Step 4>"
}
```

**Signing:** Signed with ES256 using a private key (loaded from `test/helpers/credential/privateKey`).

Key points for the pentester:
- `sub` is the wallet's subject identifier — the issuer checks this matches what was in the original credential offer.
- `c_nonce` binds this access token to the proof JWT below.
- `aud` is set to the issuer (from `pre-authorized_code.iss`) — the issuer validates it's the intended audience.
- `credential_identifiers` tells the issuer which specific credentials to return.

---

## Step 6: Construct the Proof JWT (Proof of Possession)

The test creates a `did:key` from the public key (compressed EC point, multicodec-prefixed, base58-encoded) and then builds a proof JWT:

**Header:**
```json
{
  "alg": "ES256",
  "kid": "did:key:z<base58-encoded-public-key>",
  "typ": "openid4vci-proof+jwt"
}
```

**Payload:**
```json
{
  "iss": "urn:fdc:gov:uk:wallet",
  "aud": "<pre-authorized_code.iss>",
  "iat": "<current timestamp>",
  "nonce": "<the same nonce from Step 4>"
}
```

**Signing:** Signed with ES256 using the same private key.

Key points for the pentester:
- The `nonce` in the proof MUST match the `c_nonce` in the access token — the issuer rejects mismatches with `invalid_nonce`.
- The `kid` in the header is a `did:key` encoding of the public key — the issuer uses this to verify the proof JWT's signature.
- The `iss` is always `urn:fdc:gov:uk:wallet` (fixed wallet identifier).
- The `aud` must match the credential issuer.

---

## Step 7: Call the /credential Endpoint

The test makes the following HTTP request:

```
POST {credential_endpoint}
Authorization: Bearer <access_token from Step 5>
Content-Type: application/json

{
  "proof": {
    "proof_type": "jwt",
    "jwt": "<proof JWT from Step 6>"
  }
}
```

---

## Step 8: Validate the Response

A successful response (HTTP 200) returns:

```json
{
  "credentials": [
    {
      "credential": "<JWT or mDL CBOR>"
    }
  ],
  "notification_id": "<optional, for lifecycle events>"
}
```

The test then verifies:
- For JWT credentials: validates the signature against the issuer's `did:web` document verification methods.
- For mDOC credentials: validates against the IACA root certificate from the `/iacas` endpoint.

---

## Error Cases Tested

| Scenario | Expected Response |
|----------|------------------|
| Access token `sub` doesn't match the wallet subject in the offer | `401` + `WWW-Authenticate: invalid_token` |
| Access token signature is tampered with | `401` + `WWW-Authenticate: invalid_token` |
| Proof JWT signature is tampered with | `400` + `{"error": "invalid_proof"}` |
| Proof JWT nonce doesn't match access token `c_nonce` | `400` + `{"error": "invalid_nonce"}` |
| Same credential offer redeemed a second time | `401` + `WWW-Authenticate: invalid_token` |

---

## Credential Refresh Flow

A refresh request is identical to the happy path except the access token is created **without** `credential_identifiers` in the payload. The issuer still returns a valid credential.

---

## Notification Endpoint (Optional)

After receiving a credential, the wallet can notify the issuer of lifecycle events:

```
POST {notification_endpoint}
Authorization: Bearer <same access token>
Content-Type: application/json

{
  "notification_id": "<from credential response>",
  "event": "credential_accepted" | "credential_deleted" | "credential_failure"
}
```

- Valid notifications return `204 No Content`.
- Missing `notification_id` or invalid event types return `400`.
- Invalid/missing auth returns `401`.

---

## Key Files

| File | Purpose |
|------|---------|
| `test/helpers/credential/privateKey` | EC P-256 private key (JWK) used to sign access tokens and proof JWTs |
| `test/helpers/credential/publicKey` | Corresponding public key (JWK) used to create the `did:key` |
| `test/helpers/api/api.ts` | HTTP client functions (`getCredential`, `sendNotification`, etc.) |
| `test/helpers/credential/createAccessToken.ts` | Access token construction logic |
| `test/helpers/credential/createProofJwt.ts` | Proof JWT and `did:key` construction logic |
