# mDL - Document Signing Certificate Issuer

## Overview

This document signing certificate issuer is a reference implementation. It shows you how to issue the X.509 document signing certificates that you need for issuing an mdoc credential to GOV.UK Wallet.

This code is for government departments and service teams who want to issue credentials to GOV.UK Wallet. It should not be used in production.

The `template.yaml` in this project deploys:

- a Lambda function to issue an X.509 document signing certificate using an AWS Private CA instance deployed by the `mobile-wallet-onboarding-products-infra/platform-ca` CloudFormation stack in the account
- an asymmetric ECC_NIST_P256 key management service (KMS) key to act as the document signing key and securely manage the key material and signing function
- an S3 bucket to store the root certificate and the issued document signing certificates in PEM format so they can be accessed by the [example credential issuer](https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer/tree/main/example-credential-issuer)

### Lifecycle

This template will issue one certificate for the deployed KMS key when you invoke the Lambda. By default, this key has a lifecycle of 760 days, or about 2 years and 1 month.
If you invoke the issuing Lambda again, it will not issue more certificates for the same key.

To rotate a certificate:

1. create a new KMS resource in the template.
2. update the Lambda environment variable to refer to the new key.

When no more documents should be issued signed by a key, you should remove the associated KMS resource from the template. When you deploy this change, AWS will mark the key for deletion and destroy the private key material.

### Output

The certificate issuer stores the certificates it outputs in an S3 bucket with the key `sign/<keyId>/certificate.pem`, where:

- `<keyId>` is the KMS key ID in the form of a UUIDv4 string
- `certificate.pem` is the canonical representation of the certificate and should be used in certificate path validation

The certificate issuer uploads the root certificate to the same bucket with the key `root/<keyId>/certificate.pem`, where `<keyId>` is the certificate authority ID (in the form of a UUIDv4 string).

## Disclaimers

- This reference implementation is an informative guide, not a production ready asset. You must perform sufficient engineering and additional testing to properly evaluate your application.
- You should check that you are using the latest version of this implementation.
- This implementation may change, add or remove features, which may make it incompatible with your code.
- This implementation is limited in scope.

## Contact us

If you have questions or suggestions, contact us on [govukwallet-queries@digital.cabinet-office.gov.uk](mailto:govukwallet-queries@digital.cabinet-office.gov.uk) or use #govuk-wallet in x-gov Slack.

## Tech stack

This service is built with TypeScript and Node.js, deployed as an AWS Lambda function via AWS SAM. It uses S3 for certificate storage, KMS for signing, and SSM Parameter Store for configuration management.

## Prerequisites

- [Node.js](https://nodejs.org/en) — we recommend managing versions with [nvm](https://github.com/nvm-sh/nvm)
- [Pre-commit](https://pre-commit.com/)

## Set up locally

### Install

```bash
npm install
```

### Lint and format

```bash
npm run lint:fix
npm run format
```

### Infrastructure static analysis

```bash
npm run checkov
```

### Test

```bash
npm run test
```

## Deploy

This service is deployed via GitHub Actions, or using the AWS SAM CLI.

Automated deployments to `build` are triggered on push to `main` after PR approval. Manual deployments to `dev` can be triggered from the GitHub Actions menu, where you can specify a branch name or commit SHA.

## Contribute

[README](../README.md)
