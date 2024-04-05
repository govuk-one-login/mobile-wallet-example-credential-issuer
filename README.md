# mobile-wallet-example-credential-issuer

## Overview
A credential Issuer following the [OpenID for Verifiable Credential Issuance v1.0; pre-authorized code flow](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-pre-authorized-code-flow) to issue credentials into the GOV.UK wallet.

## Pre-requisites

### SDKMan
This project has an `.sdkmanrc` file.

Install SDKMan via the instructions on `https://sdkman.io/install`.

For auto-switching between JDK versions, edit your `~/.sdkman/etc/config` and set `sdkman_auto_env=true`.

Then use sdkman to install Java JDK listed in this project's `.sdkmanrc`, e.g. `sdk install java x.y.z-amzn`.

Restart your terminal.

### Gradle
Gradle 8 is used on this project.

## Quickstart

### Linting

Check with `./gradlew spotlessCheck`

Apply with `./gradlew spotlessApply`

### Build
Build with `./gradlew`

By default, this also calls `clean`,  `spotlessApply` and `test`.

### Run

#### Setting up the AWS CLI
You will need to have the [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) installed and configured to interact with the local database. You can configure the CLI with the values below by running `aws configure`:
```
AWS Access Key ID [None]: na
AWS Secret Access Key [None]: na
Default region name [None]: eu-west-2
Default output format [None]:
```

####  Setting up LocalStack
This app uses LocalStack to run AWS services locally on port `4560`.

To start the LocalStack container and provision a local version of KMS and the **cri_cache** DynamoDB table , run `docker-compose up`.

You will need to have Docker Desktop or alternative like installed.

#### Running the Application
Run the application with `./gradlew run`

#### Test API Request
To get a credential offer:
```
curl -X GET http://localhost:8080/credential_offer?walletSubjectId=walletSubjectIdPlaceholder&documentId=testDocumentId&credentialType=BasicCheckCredential | jq
```

#### Reading from the Database
To check that a credential offer was saved to the **cri_cache** table, run `aws --endpoint-url=http://localhost:4560 --region eu-west-2 dynamodb query --table-name cri_cache --key-condition-expression "credentialIdentifier = :credentialIdentifier" --expression-attribute-values "{ \":credentialIdentifier\" : { \"S\" : \"e457f329-923c-4eb6-85ca-ee7e04b3e173\" } }"`, replacing the **credentialIdentifier** with the relevant one.

To return all items from the **cri_cache** table, run `aws --endpoint-url=http://localhost:4560 --region eu-west-2 dynamodb scan --table-name cri_cache"`.

### Test
Run unit tests with `./gradlew test`
