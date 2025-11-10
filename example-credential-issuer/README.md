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
| ------------------------------------------------------------------------------------------------------------------------ | ------------------------------ |
| Issuing an [ISO_mDL](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-ID2.html#section-1) credential | ✅  Yes                         |
| Issuing a [W3C VCDM](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-ID2.html#section-1) credential | ✅ Yes                          |
| Issuing a SD-JWT-VC credential                                                                                           | ❌ This format is not supported |
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

## Maintain the credential issuer

These instructions are for GOV.UK Wallet developers who are maintaining this service.

### Before you start

You must install:

* Java
* Gradle
* a tool for running docker applications locally, like [Docker Desktop](https://www.docker.com/products/docker-desktop/)
*[AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)

### Run the credential issuer

> Ensure that you are using the Java/Gradle versions specified in `.sdkmanrc`.

#### Format

Check with `./gradlew spotlessCheck`

Apply with `./gradlew spotlessApply`

#### Build

Build with `./gradlew`

By default, this also calls `clean`, `spotlessApply` and `test`.

#### Run

##### Set up LocalStack

This app uses LocalStack to run AWS services (DynamoDB and KMS) locally on port `4560`.

To start the LocalStack container and emulate the services, run:

```
docker compose -f docker-compose.yml up --build -d --wait
```

##### Run the Application

Run with `./gradlew run`

#### Test

##### Unit Tests

Run unit tests with `./gradlew test`

##### Testing with the Example CRI Test Harness

The [test harness](https://github.com/govuk-one-login/mobile-wallet-cri-test-harness) contains a mock of the GOV.UK One Login authorization server. You must configure your authorization server URL to point to the mock:

`ONE_LOGIN_AUTH_SERVER_URL=http://localhost:3001 ./gradlew run`.

### Deploy application to `dev`

> You must be logged into the Mobile Platform `dev` AWS account.

You can deploy the application to the `dev` AWS account by following these steps.

#### Build and push the docker image

Run the script to build and push the Example CRI docker image. Make sure to specify:

* an image tag
* the name of your AWS profile for the Mobile Platform `dev` AWS account (which can be found in your `~/.aws/credentials` file)

```shell
./build-and-deploy-image.sh <your-chosen-tag> <your-mobile-platform-dev-profile> 
```

This will:

* build the docker image
* log into ECR
* push the image to ECR
* update the `template.yaml` to specify this image for the Example CRI ECS task

#### Update the SAM template

If using your own deployed version of the [Document Builder](https://github.com/govuk-one-login/mobile-wallet-document-builder), you must update the following mapping values in the template:

```yaml
Mappings:
  EnvironmentVariables:
    dev:
      CredentialStoreUrl: "<stack-name->stub-credential-issuer.mobile.dev.account.gov.uk"
      AuthServerUrl: "<stack-name->stub-credential-issuer.mobile.dev.account.gov.uk"
```

#### Build and deploy the stack
Run:

```bash
sam build && sam deploy --capabilities CAPABILITY_IAM --stack-name <your_stack_name>
```
