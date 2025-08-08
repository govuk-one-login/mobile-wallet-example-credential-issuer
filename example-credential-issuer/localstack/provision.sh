#!/bin/sh

export CREDENTIAL_OFFER_TABLE_NAME=credential_offer_cache_2
export CREDENTIAL_TABLE_NAME=credential_store_2

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
    --attribute-definitions AttributeName=credentialIdentifier,AttributeType=S \
    --key-schema AttributeName=credentialIdentifier,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --region eu-west-2

aws --endpoint-url=http://localhost:4566 dynamodb update-time-to-live --table-name $CREDENTIAL_TABLE_NAME \
                      --time-to-live-specification Enabled=true,AttributeName=timeToLive

aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"ff275b92-0def-4dfc-b0f6-87c96b26c6c7"}]' # signing key

aws --endpoint-url=http://localhost:4566 kms create-alias \
    --region eu-west-2 \
    --alias-name alias/localSigningKeyAlias \
    --target-key-id ff275b92-0def-4dfc-b0f6-87c96b26c6c7

aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4"}]' # document signing key

aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket certificates --create-bucket-configuration LocationConstraint=eu-west-2 --region eu-west-2

cat <<EOF > certificate.pem
-----BEGIN CERTIFICATE-----
MIICzzCCAnWgAwIBAgIUFBD7/XkDw4D/UTy7/pf1Q7c43/kwCgYIKoZIzj0EAwIw
gbwxCzAJBgNVBAYTAlVLMQ8wDQYDVQQIDAZMb25kb24xNDAyBgNVBAoMK21ETCBF
eGFtcGxlIElBQ0EgUm9vdCAtIERFTE9DQUwgZW52aXJvbm1lbnQxMjAwBgNVBAsM
KW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIExPQ0FMIGVudmlyb25tZW50MTIwMAYD
VQQDDCltREwgRXhhbXBsZSBJQUNBIFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDAe
Fw0yNTA2MTkxMTA4NTFaFw0zNTA2MTcxMTA4NTFaMIG8MQswCQYDVQQGEwJVSzEP
MA0GA1UECAwGTG9uZG9uMTQwMgYDVQQKDCttREwgRXhhbXBsZSBJQUNBIFJvb3Qg
LSBERUxPQ0FMIGVudmlyb25tZW50MTIwMAYDVQQLDCltREwgRXhhbXBsZSBJQUNB
IFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDEyMDAGA1UEAwwpbURMIEV4YW1wbGUg
SUFDQSBSb290IC0gTE9DQUwgZW52aXJvbm1lbnQwWTATBgcqhkjOPQIBBggqhkjO
PQMBBwNCAATK8ZrETZ7FQXw3+xj7fLV2yv1vFLOlZE0r2MQ0ysBOa/uZ7dUlOCvR
OTt5fpDR9e+Hdq0h9trZwwBY2HODAWVbo1MwUTAdBgNVHQ4EFgQUnelQVCApK3NI
xVeQ3X+zUsogQxgwHwYDVR0jBBgwFoAUnelQVCApK3NIxVeQ3X+zUsogQxgwDwYD
VR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNIADBFAiBwnpi6jeCSLxZgFeFLSN+z
aG3zj9t6QcGFklY521tMtQIhAOF65mV0uski5+50FtKkJcVnS/1EDGrgor5bFeZD
vdAI
-----END CERTIFICATE-----
EOF

aws --endpoint-url=http://localhost:4566 s3 cp certificate.pem s3://certificates/6bb42872-f4ed-4d55-a937-b8ffb8760de4/ --region eu-west-2 # root certificate

aws --endpoint-url=http://localhost:4566 s3 cp certificate.pem s3://certificates/1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4/ --region eu-west-2 # document signing certificate
