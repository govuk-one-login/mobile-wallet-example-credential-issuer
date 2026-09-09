#!/bin/sh

export CREDENTIAL_OFFER_TABLE_NAME=credential_offer_cache
export CREDENTIAL_TABLE_NAME=credential_store

aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name $CREDENTIAL_OFFER_TABLE_NAME \
    --attribute-definitions AttributeName=credentialIdentifier,AttributeType=S \
    --key-schema AttributeName=credentialIdentifier,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --region eu-west-2

aws --endpoint-url=http://localhost:4566 dynamodb update-time-to-live --table-name $CREDENTIAL_OFFER_TABLE_NAME \
                      --time-to-live-specification Enabled=true,AttributeName=timeToLive

aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name $CREDENTIAL_TABLE_NAME \
    --attribute-definitions AttributeName=credentialIdentifier,AttributeType=S AttributeName=documentId,AttributeType=S \
    --key-schema AttributeName=credentialIdentifier,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --region eu-west-2 \
    --global-secondary-indexes \
            "[
                {
                    \"IndexName\": \"documentIdIndex\",
                    \"KeySchema\": [
                        {\"AttributeName\":\"documentId\",\"KeyType\":\"HASH\"}
                    ],
                    \"Projection\": {
                        \"ProjectionType\":\"ALL\"
                    },
                    \"ProvisionedThroughput\": {
                        \"ReadCapacityUnits\": 1,
                        \"WriteCapacityUnits\": 1
                    }
                }
            ]"

aws --endpoint-url=http://localhost:4566 dynamodb update-time-to-live --table-name $CREDENTIAL_TABLE_NAME \
                      --time-to-live-specification Enabled=true,AttributeName=timeToLive

aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket certificates --create-bucket-configuration LocationConstraint=eu-west-2 --region eu-west-2

# Root certificate.
cat <<EOF > root-certificate.pem
-----BEGIN CERTIFICATE-----
MIIB2DCCAX+gAwIBAgIUBKGlQUZiouB9C9Rcsj65CeDX/iEwCgYIKoZIzj0EAwIw
QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
IExPQ0FMIGVudmlyb25tZW50MCAXDTI2MDkwODIwMDAyNloYDzIxMzYwMzE1MjAw
MDI2WjBBMQswCQYDVQQGEwJHQjEyMDAGA1UEAwwpbURMIEV4YW1wbGUgSUFDQSBS
b290IC0gTE9DQUwgZW52aXJvbm1lbnQwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNC
AAQP+/8ZSuDGzm/oCCC+7VwOzKEOEg2wIMc26sTx+uE0Og0RMX3oa/8gwCfgbDAh
tvzP/qQWyFHP5kFiM/mWRIL5o1MwUTAdBgNVHQ4EFgQU8a3zT5qMoVmtvIxRdOxi
beMTCfwwHwYDVR0jBBgwFoAU8a3zT5qMoVmtvIxRdOxibeMTCfwwDwYDVR0TAQH/
BAUwAwEB/zAKBggqhkjOPQQDAgNHADBEAiAhPtLnTX5tnEBvMIRrWsmLyr7LLBu+
0yX/7AYf86INKAIgUIaFWxKUq0ep2wnWNW8HXJK8vMmn7SsA+JzA9xoxkXs=
-----END CERTIFICATE-----
EOF

# Upload root certificate to S3
aws --endpoint-url=http://localhost:4566 s3 cp root-certificate.pem s3://certificates/root/6bb42872-f4ed-4d55-a937-b8ffb8760de4/certificate.pem --region eu-west-2

# Document signing certificate containing the public key from the mDoc signing key pair, signed by the root certificate
cat <<EOF > document-signing-certificate.pem
-----BEGIN CERTIFICATE-----
MIIBujCCAV+gAwIBAgIUXhYiEGtTIXDrwWCNwdUJXcWw1jMwCgYIKoZIzj0EAwIw
QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
IExPQ0FMIGVudmlyb25tZW50MCAXDTI2MDkwODIwMDM1OVoYDzIxMzYwMzE1MjAw
MzU5WjAyMQswCQYDVQQGEwJHQjEjMCEGA1UEAwwaRXhhbXBsZSBJc3N1ZXIgRFND
IChMT0NBTCkwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAT40qLhClhX9q+pMUWi
V1EhgRlpGAoX+q/7Emx6TrcSHZwqf3bbOUkePYqCXNKUubjf0F8VgYNhV63pbumS
8hh+o0IwQDAdBgNVHQ4EFgQUUEFaKCPaFeO4k2J0HFMU+8bHM4swHwYDVR0jBBgw
FoAU8a3zT5qMoVmtvIxRdOxibeMTCfwwCgYIKoZIzj0EAwIDSQAwRgIhAKeIMDWJ
aF8YmAsH6sALj3t1+yfry37Ylc/pVf53w5F1AiEAwMtpQcdDtR8bLUMyYHcV5GsR
G+yi4Szy8mU9iCBIkjo=
-----END CERTIFICATE-----
EOF

# Upload document signing certificate to S3
aws --endpoint-url=http://localhost:4566 s3 cp document-signing-certificate.pem s3://certificates/sign/1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4/certificate.pem --region eu-west-2
