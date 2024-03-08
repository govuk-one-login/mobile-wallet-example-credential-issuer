~~# mobile-wallet-example-credential-issuer

## Overview

A Mock Wallet CRI stub for testing the GOV.UK wallet.

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

Check with `./gradlew spotlessCheck`.

Apply with `./gradlew spotlessApply`.

### Build
Build with `./gradlew`

By default, this also calls `clean` and `spotlessApply`.

### Run
This app uses LocalStack to run AWS services locally. To start the LocalStack container and provision a local version of KMS and the **cri_cache** DynamoDB table, run the command:
```
docker-compose up
```

Then run the application with `./gradlew run`.

Open [http://localhost:8080/credential_offer?walletSubjectId=123abc&documentId=456def](http://localhost:8080/credential_offer?walletSubjectId=123abc&documentId=456def) in a web browser.

Check that the credential offer was saved to the **cri_cache** by running the following command in the terminal (replacing the `credentialIdentifier` in the command):
```
aws --endpoint-url=http://localhost:4566 --region eu-west-2 dynamodb query --table-name cri_cache --key-condition-expression "credentialIdentifier = :credentialIdentifier" --expression-attribute-values "{ \":credentialIdentifier\" : { \"S\" : \"e457f329-923c-4eb6-85ca-ee7e04b3e173\" } }"
```

Return all items from **cri_cache** with the following command:
```
aws --endpoint-url=http://localhost:4566 --region eu-west-2 dynamodb scan --table-name cri_cache"
```

#### AWS CLI

Your AWS CLI options must be configured before running this command. If they are not, run ```aws configure``` before running the commands above.