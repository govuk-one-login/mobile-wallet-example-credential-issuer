# 04 — Decode the mDL credential

The `credential` returned by [`03`](./03-call-credential-endpoint.md) is a base64url-encoded
CBOR mDoc. Decode it to extract the two values that drive the later journeys:

- `document_number` → the `documentId` for revoke ([`05`](./05-revoke-credential.md))
- `status_list.uri` and `status_list.idx` → the status list read for sharing
  ([`06`](./06-credential-sharing-status-list.md))

Reference decoder (mDoc/CBOR path):
`document-builder/src/credentialViewer/decoders/mdocDecoder.ts`.

## Structure

The mDoc is an `IssuerSigned` CBOR map with two parts:

- `nameSpaces` — the data elements, grouped by namespace. For mDL the namespace is
  `org.iso.18013.5.1`. Each element is a CBOR tag-24 wrapper (`IssuerSignedItem`) with a
  `elementIdentifier` / `elementValue` pair.
- `issuerAuth` — a COSE `Sign1`. Its **payload** is the Mobile Security Object (MSO).

Decode steps (as in `mdocDecoder.ts`):

1. base64url-decode → CBOR-decode the mDoc (register the tag-24 decoder for nested CBOR).
2. `document_number`: read from `nameSpaces["org.iso.18013.5.1"]`, element
   `document_number` (unwrap the tag-24 item to get `elementValue`).
3. `status_list`: COSE-decode `issuerAuth`, then CBOR-decode its payload (the MSO). Read
   `status.status_list`, which is a map with `idx` (int) and `uri` (string).

## Values

| Value | Location | Used by |
|---|---|---|
| `document_number` | `nameSpaces["org.iso.18013.5.1"]` data element | revoke ([`05`](./05-revoke-credential.md)) |
| `status_list.uri` | MSO payload → `status` → `status_list` → `uri` | share ([`06`](./06-credential-sharing-status-list.md)) |
| `status_list.idx` | MSO payload → `status` → `status_list` → `idx` | share ([`06`](./06-credential-sharing-status-list.md)) |

> The `status` block is a top-level field of the MSO (alongside `docType`, `valueDigests`,
> `validityInfo`), so it lives inside the signed `issuerAuth` payload — not in
> `nameSpaces`.
