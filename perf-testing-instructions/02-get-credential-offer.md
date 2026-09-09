# 02 — CRI `/credential_offer` endpoint reference

Reference for the CRI endpoint that mints a credential offer. Used by
[`01-create-document-and-offer.md`](./01-create-document-and-offer.md) (Option A). The
document builder FE calls this same endpoint internally.

## Request

```
GET {CRI_URL}/credential_offer?walletSubjectId=...&itemId=...&credentialType=org.iso.18013.5.1.mDL
Accept: application/json
```

| Param | Required | Notes |
|---|---|---|
| `walletSubjectId` | Yes | Use a hardcoded value: `urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i`. |
| `itemId` | Yes | Pattern `^[a-zA-Z0-9_-]{10,50}$`. Must reference an existing document. |
| `credentialType` | Yes | `org.iso.18013.5.1.mDL`. |

## Response

`200` with a universal-link URL as the body:

```
https://...account.gov.uk/wallet/add?credential_offer=<URL-encoded JSON>
```

### Parsing the Credential Offer

Parse the offer URL as the FE does — see
`document-builder/src/credentialViewer/parsers/credentialOfferParser.ts` (`extractPreAuthCode`)
and `document-builder/src/credentialViewer/decoders/jwtDecoder.ts` (`safeDecodeJwt`) for
reference:

1. take the substring after `add?credential_offer=`,
2. URL-decode and JSON-parse it,
3. read
   `grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["pre-authorized_code"]`.

The `pre-authorized_code` is a JWT; its claims feed the access token and proof JWT in
[`03-call-credential-endpoint.md`](./03-call-credential-endpoint.md).

> A credential offer is **single-use**. Redeeming it twice at `/credential` returns
  `401 invalid_token`. Each perf iteration needs a fresh Step 1 + Step 2.

Decoded, the `credential_offer` JSON is:

```json
{
  "credential_issuer": "<CRI self URL>",
  "credential_configuration_ids": ["org.iso.18013.5.1.mDL"],
  "grants": {
    "urn:ietf:params:oauth:grant-type:pre-authorized_code": {
      "pre-authorized_code": "<JWT>"
    }
  }
}
```

Extract the `pre-authorized_code` JWT.

## Behaviour notes

- Each call is independent: a fresh offer id and a new, distinct `pre-authorized_code` are
  generated every time. Reusing the same `itemId` across calls is allowed and yields
  multiple independently-redeemable offers.
- The offer is bound to the `walletSubjectId` supplied here; `POST /credential`
  ([`03`](./03-call-credential-endpoint.md)) later checks the access-token `sub` matches.
- An offer is single-use at `/credential` and expires after the CRI's configured offer TTL.
- Errors: unsupported `credentialType` → `400`; signing/storage failure → `500`.
