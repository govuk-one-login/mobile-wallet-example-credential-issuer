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

To start the LocalStack container and provision a local version of KMS and the DynamoDB table where credential offers are stored, run `docker compose up`.

You will need to have Docker Desktop or alternative like installed.

#### Running the Application
Run the application with `./gradlew run`

#### Test API Request
To get a credential offer:
```
curl -X GET http://localhost:8080/credential_offer?walletSubjectId=urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i&documentId=testDocumentId&credentialType=BasicCheckCredential | jq
```

To get the credential metadata:
```
curl -X GET http://localhost:8080/.well-known/openid-credential-issuer | jq
```

To get a credential (replace the proof JWT and bearer access token values before testing):
```
curl -d '{"proof":{"proof_type":"jwt", "jwt": "<<insert proof jwt>>" }}' -H "Content-Type: application/json" -H "Authorization: Bearer <<insert bearer token jwt>>" -X POST http://localhost:8080/credential | jq
```

To get the DID document:
```
curl -X GET http://localhost:8080/.well-known/did.json | jq
```

To get the JWKS:
```
curl -X GET http://localhost:8080/.well-known/jwks.json | jq
```

#### Reading from the Database
To check that a credential offer was saved to the table, run:

`aws --endpoint-url=http://localhost:4560 --region eu-west-2 dynamodb query --table-name credential_offer_cache --key-condition-expression "credentialIdentifier = :credentialIdentifier" --expression-attribute-values "{ \":credentialIdentifier\" : { \"S\" : \"e457f329-923c-4eb6-85ca-ee7e04b3e173\" } }"`

replacing the **credentialIdentifier** with the relevant one.

To return all items from the table, run:

 `aws --endpoint-url=http://localhost:4560 --region eu-west-2 dynamodb scan --table-name credential_offer_cache`.

### Test
#### Unit Tests
Run unit tests with `./gradlew test`

#### Testing with the Example CRI Test Harness
When testing with the [test harness](https://github.com/govuk-one-login/mobile-wallet-cri-test-harness) locally, you must point the authorization server to the right address:
```
ONE_LOGIN_AUTH_SERVER_URL=http://localhost:3001 ./gradlew run  
```
