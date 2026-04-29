# GOV.UK Wallet example credential issuer

## Overview

This example credential issuer demonstrates how to implement the OID4VCI pre-authorized code flow to issue verifiable credentials to GOV.UK Wallet. Our development team uses this example credential issuer to test GOV.UK Wallet.

This code is for government departments and service teams who want to issue credentials to GOV.UK Wallet. It is a reference implementation that can help you integrate with GOV.UK Wallet, and should not be used in production.

This implementation follows the pre-authorized code flow as specified in the [GOV.UK Wallet technical documentation](https://docs.wallet.service.gov.uk/).

You can use the example credential issuer to:

* review the APIs that you need to build
* understand how to create a credential offer
* publish your own metadata and endpoints
* issue a credential

There is more guidance on GOV.UK Wallet in the [technical documentation](https://docs.wallet.service.gov.uk/).

## What is supported/not supported

| Feature                                                                                                                  | Support                        |
|--------------------------------------------------------------------------------------------------------------------------| ------------------------------ |
| Issuing an [ISO mDL](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-ID2.html#section-1) credential | ✅  Yes                         |
| Issuing a [W3C VCDM](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-ID2.html#section-1) credential | ✅ Yes                          |
| Issuing a SD-JWT VC credential                                                                                           | ❌ This format is not supported |
| Revoking a credential with the status list                                                                               | ✅ Yes                          |

This example credential issuer does not connect to any real data sources. To use this example credential issuer, you need to modify its code to connect to your existing data sources and systems that contain the information required for the credentials you wish to issue.

You must not use this example credential issuer in production.

## Disclaimers

* This reference implementation is an informative guide, not a production ready asset. You must perform sufficient engineering and additional testing to properly evaluate your application and determine whether any of the open-sourced components are suitable for use in that application.
* You should check that you are using the latest version of this implementation.
* This implementation may change, add or remove features, which may make it incompatible with your code.
* This implementation is limited in scope.

## Contact us

If you have questions or suggestions, contact us on [govukwallet-queries@digital.cabinet-office.gov.uk](mailto:govukwallet-queries@digital.cabinet-office.gov.uk) or use #govuk-wallet in x-gov Slack.

## Tech Stack

This service is built with Java using Gradle, containerised with Docker, and deployed to ECS Fargate behind an API Gateway. It uses DynamoDB for storage and KMS for signing and encryption, with infrastructure managed via AWS SAM.

## Prerequisites

* Java 17
* Gradle 8.8
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) — required to run LocalStack locally and to build the app image
* [Pre-commit](https://pre-commit.com/)

## Local Setup

### Format

`./gradlew spotlessApply`

### Build

`./gradlew`

By default, this also calls `clean`, `spotlessApply` and `test`.

### Run

Start LocalStack to emulate AWS services (DynamoDB and KMS) locally on port `4560`.

```
./gradlew localstackUp
```

Running locally also requires a mock of STS and status list for end-to-end journey functionality.

Start the application:

`./gradlew run`

The service will be available at http://localhost:8080.

### Test

`./gradlew test`

### Infrastructure static analysis

`./gradlew checkov`

##### Testing with the Example CRI Test Harness

The [test harness](https://github.com/govuk-one-login/mobile-wallet-cri-test-harness) contains a mock of the GOV.UK One Login authorization server. You must configure your authorization server URL to point to the mock:

`ONE_LOGIN_AUTH_SERVER_URL=http://localhost:3001 ONE_LOGIN_AUTH_SERVER_URLS=http://localhost:3001 ENVIRONMENT=ci ./gradlew run`.

## Deployment

This service is deployed via GitHub Actions.

Automated deployments to `build` are triggered on push to `main` after PR approval. Manual deployments to `dev` can be triggered from the GitHub Actions menu, where you can specify a branch name or commit SHA.

## Contributing

[README.md](../README.md)

## Further Documentation

| Document                                           | Description |
|----------------------------------------------------|---|
| [`docs/infrastructure.md`](docs/infrastructure.md) | Infrastructure diagram — AWS architecture, API routes, data flow |