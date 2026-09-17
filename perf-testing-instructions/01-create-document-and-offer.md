# 01 — Create the DVS mDL document and get the credential offer

Goal: obtain an `itemId` for a freshly-created DVS mDL document, then a credential offer
containing the `pre-authorized_code` needed to call `POST /credential`
([`03-call-credential-endpoint.md`](./03-call-credential-endpoint.md)).

## Step 1 — Create the document (FE)

```
GET {DOC_BUILDER_URL}/dvs/build-driving-licence
```

The handler:

- generates a random `itemId` (UUID),
- builds default DVS driving-licence data `document_number` is assigned automatically,
- redirects (HTTP 302) to the offer viewer:

  ```
  /dvs/view-credential-offer/{itemId}?type=org.iso.18013.5.1.mDL
  ```

Extract `itemId` from the `Location` header of the redirect. This is a GET with no body;
each call creates a new document.


## Step 2 — Get the credential offer

You have two options.

### Option A — call the CRI directly

```
GET {CRI_URL}/credential_offer?walletSubjectId=...&itemId=...&credentialType=org.iso.18013.5.1.mDL
```

Full endpoint contract (params, headers, response) is in
[`02-get-credential-offer.md`](./02-get-credential-offer.md). 

The response is a universal-link URL of the form:

```
https://...account.gov.uk/wallet/add?credential_offer=<URL-encoded JSON>
```

### Option B — scrape the FE offer viewer
```
GET {DOC_BUILDER_URL}/dvs/build-driving-licence
```
redirects to
```
GET {DOC_BUILDER_URL}/dvs/view-credential-offer/{itemId}?type=org.iso.18013.5.1.mDL
```

Handler on loads:

- calls the CRI `GET /credential_offer` (same as Option A),
- renders `dvs-credential-offer.njk`, embedding the offer URL in both the **QR code** and
  the **"Add document" button** (`universalLink`).

To use this path, scrape the `universalLink` from the `href` of the Add document button (or
decode the QR). Note the offer is valid for **15 minutes**.
