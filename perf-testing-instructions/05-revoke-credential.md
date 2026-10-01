# 05 — Revoke the credential

Revoke all credentials issued for a document, keyed by its `documentId` (the
`document_number` extracted in [`04`](./04-decode-credential.md)).

Handler: `example-credential-issuer/.../revoke/RevokeResource.java`.

## Request

```
POST {CRI_URL}/revoke/{documentId}
```

- No body, no auth.
- `documentId` pattern: `^[a-zA-Z0-9]{5,25}$`.

The CRI looks up every stored credential for that `documentId` and revokes each against the
status list service (see [`06`](./06-credential-sharing-status-list.md)).

## Responses

| Status | Meaning |
|---|---|
| `202` | Accepted — revocation processed. |
| `404` | No credential found for the `documentId`. |
| `500` | One or more credentials failed to revoke, or a lookup error. |

## Notes

- Revocation is per `documentId`, not per credential offer. If an `itemId`/document was
  issued multiple times, one revoke call targets all of its credentials.
- Revoke counts against the DVS "credentials issued or revoked" limit (see the README
  table).
