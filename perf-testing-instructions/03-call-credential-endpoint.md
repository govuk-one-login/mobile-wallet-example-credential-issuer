# 03 — Call the CRI `/credential` endpoint

Redeem the `pre-authorized_code` from
[`01-create-document-and-offer.md`](./01-create-document-and-offer.md) for an mDL
credential. The wallet's behaviour (self-signed access token + proof-of-possession JWT) is
already solved in the test harness — reference it rather than re-deriving:

- `test-harness/test/helpers/credential/createAccessToken.ts`
- `test-harness/test/helpers/credential/createProofJwt.ts`
- Orchestrator - `test-harness/test/credential-issuer-tests.test.ts` line 391
- Functional walkthrough: [`how-to-call-credential-api.md`](./how-to-call-credential-api.md)

We run the test harness, but it uses many mocks -
- CI driver: `.github/workflows/example-credential-issuer-integration-tests.yml`


## `integration` nuance — read first

In the `integration` (DVS Sandbox) the CRI **skips access-token signature verification**
(`AccessTokenService.isSignatureVerificationSkipped()` is true for
`local`, `dev`, `build`, `integration`). Consequences for perf scripts:

- You do **not** need to register a signing key or publish a JWKS. Any locally-generated
  ES256 key works for the access token.
- The access-token **header and claims are still validated** (see below) — a malformed
  header or wrong `iss`/`aud` still fails.
- The **proof JWT is still verified** via its embedded `did:key`, so it must be correctly
  signed by the key whose `did:key` is in its `kid`.

## How the test harness manages the signing key

The harness does not ship committed key files — it **generates the keypair at startup** and
reuses it for every request in that run:

- `test-harness/src/initialiseKeyPair.ts` (called from `src/app.ts` when the harness boots)
  runs `generateKeyPair("ES256")`, then writes the JWKs to
  `test/helpers/credential/privateKey` and `test/helpers/credential/publicKey`. These files
  are created at runtime, not stored in the repo.
- The **`kid` is fixed** (`getKeyId()` in `src/config.ts` →
  `5d76b492-d62e-46f4-a3d9-bc51e8b91ac5`). It is stamped on the public JWK and used as the
  access-token header `kid`.
- The tests (`credential-issuer-tests.test.ts`) read those two files once in `beforeAll`
  into `PRIVATE_KEY_JWK` / `PUBLIC_KEY_JWK`, then **reuse the same key for every test case**
  in the run — the access token is signed with the private key, and the proof JWT's
  `did:key` (`createDidKey`) is derived from the public key.
- The harness also serves its public key at its own `GET /.well-known/jwks.json`, keyed by
  the fixed `kid`. On a signature-verifying environment the CRI could resolve the key by
  `kid` and check the access-token signature; on `integration` this step is skipped.

**For perf scripts:** mirror this — generate one ES256 (P-256) keypair at script startup,
assign it a stable `kid`, and reuse it across all iterations (keep it in memory; you do not
need those file paths). Only the per-request `c_nonce` and the offer change per iteration.

## Flow

1. Decode the `pre-authorized_code` JWT (no verification) to read `iss`, `aud`,
   `credential_configuration_ids`, `credential_identifiers`.
2. `GET {CRI_URL}/.well-known/openid-credential-issuer` → take `credential_endpoint`.
3. Generate a `c_nonce` (random UUID) shared between the access token and proof JWT.
4. Build the **access token** (ES256) (`createAccessToken.ts`):
   - header: `alg=ES256`, `typ=at+jwt`, `kid=<any non-null>`
   - claims: `sub=<walletSubjectId from the offer>`, `iss=<pre-auth aud>`,
     `aud=<pre-auth iss>`, `credential_configuration_ids`, `credential_identifiers`,
     `c_nonce`, `exp`, `jti`
5. Build the **proof JWT** (ES256) (`createProofJwt.ts`) (:
   - header: `alg=ES256`, `typ=openid4vci-proof+jwt`, `kid=did:key:z...` (of the same key)
   - claims: `iss=urn:fdc:gov:uk:wallet`, `aud=<pre-auth iss>`, `iat`, `nonce=<c_nonce>`
6. POST:
   ```
   POST {credential_endpoint}
   Authorization: Bearer <access token>
   Content-Type: application/json

   { "proof": { "proof_type": "jwt", "jwt": "<proof JWT>" } }
   ```

## Response

`200`:

```json
{ "credentials": [ { "credential": "<mDL, mDoc/CBOR>" } ], "notification_id": "<optional>" }
```

The mDL `credential` is base64url-encoded CBOR — decode it in
[`04-decode-credential.md`](./04-decode-credential.md).

## Notes

- `sub` must match the `walletSubjectId` used to mint the offer, or `401 invalid_token`.
- `nonce` (proof) must equal `c_nonce` (access token), or `400 invalid_nonce`.
- The offer is single-use: a second redemption returns `401 invalid_token` — each perf
  iteration needs a fresh offer.
