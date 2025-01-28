# mobile-wallet-example-credential-issuer

## Overview

A credential Issuer following the [OpenID for Verifiable Credential Issuance v1.0; pre-authorized code flow](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-pre-authorized-code-flow) to issue credentials into the GOV.UK wallet.

## Pre-requisites

- Install Java
- Install Gradle
- [Install Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)

This project uses [SDKMAN!](https://sdkman.io/) to manage Java versions. Follow the instructions on `https://sdkman.io/install` to install it.

Then, use SDKMAN! to install the Java JDK listed in this project's `.sdkmanrc` file, e.g. `sdk install java x.y.z-amzn`. Restart your terminal.

## Quickstart

### Format

Check with `./gradlew spotlessCheck`

Apply with `./gradlew spotlessApply`

### Build

Build with `./gradlew`

By default, this also calls `clean`, `spotlessApply` and `test`.

### Run

#### Set up LocalStack

This app uses LocalStack to run AWS services (DynamoDB and KMS) locally on port `4560`.

To start the LocalStack container and emulate the services, run:
```
docker compose -f docker-compose.yml up --build -d --wait
```

#### Run the Application

Run with `./gradlew run`

### Test

#### Unit Tests

Run unit tests with `./gradlew test`

#### Testing with the Example CRI Test Harness

When testing with the [CRI Test Harness](https://github.com/govuk-one-login/mobile-wallet-cri-test-harness), the test harness must stub the STS tokens.

Therefore, you must run the Example CRI with `ONE_LOGIN_AUTH_SERVER_URL=http://localhost:3001 ./gradlew run`. 

## Deploy application to `dev`

> You must be logged into the Mobile Platform `dev` AWS account.

You can deploy the application to the `dev` AWS account by following these steps:

### Build and push the docker image

Run the script to build and push the Example CRI docker image, specifying an image tag and the name of your AWS profile
for the Mobile Platform `dev` AWS account (which can be found in your `~/.aws/credentials` file):

```shell
./build-and-deploy-image.sh <your-chosen-tag> <your-mobile-platform-dev-profile> 
```

This will build the docker image, log into ECR, push the image to ECR, and update the `template.yaml` to specify this
image for the Example CRI ECS task.

### Update the SAM template

If using your own deployed version of the [Document Builder](https://github.com/govuk-one-login/mobile-wallet-document-builder), the following mapping values in the template must be updated:

```yaml
Mappings:
  EnvironmentVariables:
    dev:
      CredentialStoreUrl: "<stack-name->stub-credential-issuer.mobile.dev.account.gov.uk"
      AuthServerUrl: "<stack-name->stub-credential-issuer.mobile.dev.account.gov.uk"
```

### Build and deploy the stack

```bash
sam build && sam deploy --capabilities CAPABILITY_IAM --stack-name <your_stack_name>
```
