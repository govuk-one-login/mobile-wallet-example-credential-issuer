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

<<<<<<< HEAD
#### Test API Request
To get a credential offer:
```
curl -X GET http://localhost:8080/credential_offer?walletSubjectId=walletSubjectIdPlaceholder&documentId=testDocumentId&credentialType=BasicCheckCredential | jq

To get the credential metadata:
```
curl -X GET http://localhost:8080/.well-known/openid-credential-issuer | jq
```

To get a credential (replace the proof JWT and bearer access token values before testing):
 ```
curl -d '{"proof":{"proof_type":"jwt", "jwt": "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFVGNtM0xNSmp6VlR4QTJMZDdURjJsdmpGbmhBdXpqZk1CbWtsTTVCczE2MVY0RHNDK3o3ZmNRR0JHMm80ZndBdEpzWW1nTDYxNDhDOXVSRVlRN3kwR1E9PSJ9.eyJpc3MiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJhdWQiOiJ1cm46ZmRjOmdvdjp1azpleGFtcGxlLWNyZWRlbnRpYWwtaXNzdWVyIiwiaWF0IjoxNzEyMjUzMDUzNzg1LCJub25jZSI6IjlkMWRlOGE0LTMyZGUtNDdkZi05ZDc3LTNjMjkzNTQwMzVjMiJ9.TSmwBtCURa0p7xDnhHHnaSLNCaytpD16kKRkP34Fe_3wUJPX6rwMHlIwqBtswKJmwRVczsibUsp3-yKn0iePOw" }}' -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJjZWQyMmUyLWMxNWItNGUwMi1hYTVmLTdhMTBhMmVhY2NjNyJ9.eyJzdWIiOiJ3YWxsZXRTdWJqZWN0SWRQbGFjZWhvbGRlciIsImlzcyI6InVybjpmZGM6Z292OnVrOndhbGxldCIsImF1ZCI6InVybjpmZGM6Z292OnVrOmV4YW1wbGUtY3JlZGVudGlhbC1pc3N1ZXIiLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbIjdlNTIzZDFmLTZjNjctNDgzYi05MDA5LWFhMmYxNTk1MTI3ZiJdLCJjX25vbmNlIjoiOWQxZGU4YTQtMzJkZS00N2RmLTlkNzctM2MyOTM1NDAzNWMyIn0.iEOHuXPhkkU7K3c_sTezvIMxkqo-04BIMfWOvovN8JH4F9xAfguhONKPiUYfZdh4sGVZUrGa7fIt3qkVtq5YkDndTnjDw3wY9xToIf8YHtxKFHi2GFYx7MggC55uxW9Kblwotxz1cUAX1voY0Oxpb6g3LosXFBpy-84KeYcFmEW9_Dzjkls_tmrml_QOgHTGxWJ_ie6SgUqFTqkxsubty9gINognqjTRMH1AxdqAlXvfSw6cK-62tDtt_xPll7LsTZYPyLInNuESVHGqYChXSUGwKzNIzp7q7yy8isohatS5fzgMj5SbGYuM4Z2F5KIAH_5ryqltfU17aYW1jmoD6_jBIb4ggyuKt2QXsUMgOi-7GEWEMLqw0Jh_Jfb28AYK2Oi54vSpzava4PUrvjmnIKeAVrILVI1lh0fTB1Iem1QuMZ4ZbAsdLzuUiEcYTW1auPZkACAsDUCs7g0c2kQYsd09MZ8Bg_Dfk2aQotUgRjba4bUBUyfI9RKDr4pUiTfGj2zUO6OfaPE6WRZr9Kb2fWo2BP0YopWRP4uPC3qoczH8pN7AsVDR3_XSJUBOTULaj6JPY-bagsOLe4Ey_LIyrqKTXLQumOYOktTDVSBXxQySvANoQe79NNHH2y8Ik3ltkLc7Xrtavr1zVXau3csAzp8zC-6QF3ledqzOMiS0O5o" -X POST http://localhost:8080/credential | jq
 ```

#### Reading from the Database
To check that a credential offer was saved to the **cri_cache** table, run `aws --endpoint-url=http://localhost:4560 --region eu-west-2 dynamodb query --table-name cri_cache --key-condition-expression "credentialIdentifier = :credentialIdentifier" --expression-attribute-values "{ \":credentialIdentifier\" : { \"S\" : \"e457f329-923c-4eb6-85ca-ee7e04b3e173\" } }"`, replacing the **credentialIdentifier** with the relevant one.

To return all items from the **cri_cache** table, run `aws --endpoint-url=http://localhost:4560 --region eu-west-2 dynamodb scan --table-name cri_cache"`.

### Test
Run unit tests with `./gradlew test`
