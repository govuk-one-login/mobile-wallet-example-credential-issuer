# mDL - Document Signing Certificate Issuer

## Overview

This document signing certificate issuer is a reference implementation. It shows you how to issue the X.509 document signing certificates that you need for issuing an mdoc credential to GOV.UK Wallet.

This code is for government departments and service teams who want to issue credentials to GOV.UK Wallet. It should not be used in production.

## Disclaimers

- This reference implementation is an informative guide, not a production ready asset. You must perform sufficient engineering and additional testing to properly evaluate your application.
- You should check that you are using the latest version of this implementation.
- This implementation may change, add or remove features, which may make it incompatible with your code.
- This implementation is limited in scope.

## Contact us

If you have questions or suggestions, contact us on [govukwallet-queries@digital.cabinet-office.gov.uk](mailto:govukwallet-queries@digital.cabinet-office.gov.uk) or use #govuk-wallet in x-gov Slack.

## Maintaining the issuer

These instructions are for GOV.UK Wallet developers who are maintaining this service.

The `template.yaml` in this project deploys:

- a Lambda function to issue an X.509 document signing certificate using an AWS Private CA instance deployed by the `mobile-platform-infra/platform-ca` CloudFormation stack in the account
- an asymmetric ECC_NIST_P256 key management service (KMS) key to act as the document signing key and securely manage the key material and signing function
- an S3 bucket to store the root certificate and the issued document signing certificates in PEM format so they can be accessed by the [example credential issuer](https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer/tree/main/example-credential-issuer)

### Before you start

You can only deploy this stack into an account which already has the `mobile-platform-infra/platform-ca` CloudFormation stack deployed. The dependency provides the AWS Private CA resource, root certificate and references to it as SSM parameters.

### Deploying the stack

You can deploy via GitHub actions, or using the AWS SAM CLI.

#### Deploy via GitHub Actions

You can deploy directly using the "DSC Issuer - Deploy to Dev" GitHub Action workflow:

1. Go to the workflow [DSC Issuer - Deploy to Dev](https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer/actions/workflows/document-sigining-certificate-issuer-dev-deploy.yml).

2. Choose the branch you wish to deploy from the dropdown.

3. Click "Run workflow" to trigger the deployment.

#### Deploy with the AWS SAM CLI

Before deploying with the AWS SAM CLI, you must authenticate with AWS. Once authenticated, run the following commands:

To build the application:

```bash
sam build
```

To deploy to AWS:

```bash
sam deploy --guided --capabilities CAPABILITY_IAM --stack-name <your_stack_name>
```

### Invoking the Lambda

The input parameters required for the issue certificate Lambda are specified in environment variables and are deployed as part of this stack.

You can invoke the issue certificate Lambda:

- through the AWS Console
- via the AWS CLI, using the following command in a shell which has active AWS credentials available:

```bash
% aws lambda invoke --function-name YOUR_STACK_NAME-issue-doc-signing-certificate output.txt
```

### Lifecycle

This template will issue one certificate for the deployed KMS key when you invoke the Lambda. By default, this key has a lifecycle of 760 days, or about 2 years and 1 month.
If you invoke the issuing Lambda again, it will not issue more certificates for the same key.

To rotate a certificate:

1. create a new KMS resource in the template.
2. update the Lambda environment variable to refer to the new key.

When no more documents should be issued signed by a key, you should remove the associated KMS resource from the template. When you deploy this change, AWS will mark the key for deletion and destroy the private key material.

### Output

The certificate issuer stores the certificates it outputs in an S3 bucket with the key `<keyId>/certificate.pem`, where:

- `<keyId>` is the KMS key ID in the form of a UUIDv4 string
- `certificate.pem` is the canonical representation of the certificate and should be used in certificate path validation

The certificate issuer uploads the root certificate to the same bucket with the key `<keyId>/certificate.pem`, where `<keyId>` is the certificate authority ID (in the form of a UUIDv4 string).
