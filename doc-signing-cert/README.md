# mDL - Document Signing Certificate Issuer

## Introduction

The template.yaml in this project deploys the following AWS resources:

- a Lambda function to issue an X.509 Document Signing Certificate using an AWS Private CA instance deployed by the `mobile-platform-infra/platform-ca` CloudFormation stack in the account.
- an asymmetric ECC_NIST_P256 KMS key to act as the Document Signing Key and securely manage the key material and signing function
- an S3 bucket to store the issued certificates both in PEM and in a decoded JSON format so they can be accessed by the Example Credential Issuer as required

## Pre-requisites

This stack can only be deployed into an account which already has the `mobile-platform-infra/platform-ca` CloudFormation stack deployed.
The dependency provides the AWS Private CA resource, root certificate and references to it as SSM parameters.

## Invocation

The required input parameters for the issue certificate lambda are specified in environment variables and are deployed as part of this stack.
The issue certificate Lambda can be invoked either in AWS Console or using the following command-line in a shell which has active AWS credentials available:

```bash
% aws lambda invoke --function-name iaca-doc-signing-cert-ddunford-issue-doc-signing-certificate output.txt 
```

## Lifecycle

This template will issue one certificate for the deployed KMS key when the Lambda is invoked, by default this key will have a lifecycle of 760 days, approx 2 years and 1 month.
If the issuing Lambda is invoked again it will not issue a second or subsequent certificates for the same key.
To rotate a certificate a new KMS resource should be created in the template and the Lambda environment variable updated to refer to the new key.
At the point where no more documents should be issued signed by a key the associated KMS resource can be removed from the template so that when deployed AWS will mark the key for deletion and destroy the private key material.

## Output

The resulting certificates are stored in an S3 bucket with the key `<keyId>/certificate.pem` where `<keyId>` is the KMS key ID (in the form of a UUIDv4 string).
Additionally a decoded version of the certificate in JSON format is deployed into the S3 bucket with the key `<keyId>/certificate-metadata.json`.
Note however the `certificate.pem` is the canonical representation of the certificate which should be used in certificate path validation.
