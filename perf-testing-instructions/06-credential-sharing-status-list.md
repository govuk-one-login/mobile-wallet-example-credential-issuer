# 06 — Credential sharing (status list read)

"Sharing with an online verifier" resolves, for perf purposes, to reading the credential's
entry in the status list. The status list service is **not** part of this repo — it is the
status-list-mock:

- Repo: `mobile-wallet-onboarding-products-mocks/status-list-mock`
- API spec: `status-list-mock/openApiSpec/mock/api-spec.yaml`
- `integration` host: `status-list-mock.wallet-onboarding.integration.account.gov.uk`

## Inputs

From decoding the mDL ([`04`](./04-decode-credential.md)):

- `status_list.uri` — the full URL of this credential's status list.
- `status_list.idx` — this credential's index within that list.

Both sit in the MSO under `status.status_list`.

## Request

```
GET {status_list.uri}
```

`status_list.uri` already points at the status-list-mock read endpoint
(`GET /t/{statusListIdentifier}`). Response is `200` with content type
`application/statuslist+jwt` — a signed IETF Token Status List JWT.

## Reading the status

The JWT payload carries a `status_list` object with a compressed bit array (`lst`). Read
the status of this credential by looking up the bit(s) at `idx`:

- `0` = valid, `1` = revoked (per the IETF Token Status List encoding).

A credential revoked via [`05`](./05-revoke-credential.md) flips its bit at `idx` in the
list returned here.

## Notes

- Sharing counts against the DVS "credentials shared" limit (see the README table), which is
  higher than the issue/revoke limit — the status list read is the hot path to exercise.
- The read endpoint is public (no auth) and cacheable (`Cache-Control: max-age=30`); account
  for caching when designing load.
