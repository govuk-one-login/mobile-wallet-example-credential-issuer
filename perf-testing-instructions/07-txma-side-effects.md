# 07 — TxMA side-effects

Every journey (issue, revoke, share) produces TxMA audit events as a **downstream
side-effect**. Perf scripts do **not** call TxMA directly — it is exercised indirectly by
the journeys in docs 01–06. 

The TxMA endpoint is stubbed by the **txma-mock** (not part of this repo):

- Template: `mobile-wallet-onboarding-products-mocks/txma-mock/template.yaml`
- `integration` host: `txma-mock.wallet-onboarding.integration.account.gov.uk`

## What it is

An API Gateway **mock** integration:

- `POST /txma-event` — receives events, `AuthorizationType: NONE`, always returns `200`. It
  is a pure mock: nothing is stored or forwarded.
- `GET /txma-healthcheck` — liveness.

Because it does not persist events, the observable record of calls is the **API Gateway
access logs** (CloudWatch log group `/aws/apigateway/<stack>-api-gateway-access-logs`, 14-day
retention), which log method, path, status, and `requestId` per request. Use these to count
`POST /txma-event` hits and correlate them with your journey load.

## Why size it higher

Each single journey emits **multiple** TxMA events, so the TxMA event rate is a multiple of
the credential rate. When designing load, test TxMA to a **higher limit** than the DVS
issue/revoke and share thresholds (README table) — it is the highest-volume touchpoint in
the system and should not be the first thing to fall over.

## Note

To learn the exact event shapes and per-journey counts, drive an issue/revoke/share journey
against `integration` and read the txma-mock API Gateway access logs, or inspect the
platform components that emit the events (outside this repo).
