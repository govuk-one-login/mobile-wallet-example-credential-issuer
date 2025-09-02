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
    --attribute-definitions AttributeName=credentialIdentifier,AttributeType=S \
    --key-schema AttributeName=credentialIdentifier,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --region eu-west-2

aws --endpoint-url=http://localhost:4566 dynamodb update-time-to-live --table-name $CREDENTIAL_TABLE_NAME \
                      --time-to-live-specification Enabled=true,AttributeName=timeToLive

# Create signing key pair - signs JWTs and JWT-based credentials
aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"ff275b92-0def-4dfc-b0f6-87c96b26c6c7"}]'

aws --endpoint-url=http://localhost:4566 kms create-alias \
    --region eu-west-2 \
    --alias-name alias/localSigningKeyAlias \
    --target-key-id ff275b92-0def-4dfc-b0f6-87c96b26c6c7

# Create document signing certificate key pair with custom key material - signs mdoc credentials
aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_key_material_","TagValue":"MHcCAQEEIKexbdPE2TDYzOuasfwN4QWNqHF1wNsV30ERMPPaRYnWoAoGCCqGSM49AwEHoUQDQgAE+NKi4QpYV/avqTFFoldRIYEZaRgKF/qv+xJsek63Eh2cKn922zlJHj2KglzSlLm439BfFYGDYVet6W7pkvIYfg=="},{"TagKey":"_custom_id_","TagValue":"1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4"}]'

aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket certificates --create-bucket-configuration LocationConstraint=eu-west-2 --region eu-west-2

cat <<EOF > root-certificate.pem
-----BEGIN CERTIFICATE-----
MIIB1zCCAX2gAwIBAgIUIatAsTQsYXy6Wrb1Cdp8tJ3RLC0wCgYIKoZIzj0EAwIw
QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
IExPQ0FMIGVudmlyb25tZW50MB4XDTI1MDkwMjEwMjQyNVoXDTI4MDYyMjEwMjQy
NVowQTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9v
dCAtIExPQ0FMIGVudmlyb25tZW50MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE
mBxJk2MqFKn7c4MSEwlA8EUbMMxyU8DnPXwERUs4VjBF7534WDQQLCZBxvaYn73M
35NYkWiXO8oiRmWG9AzDn6NTMFEwHQYDVR0OBBYEFPY4eri7CuGrxh14YMTQe1qn
BVjoMB8GA1UdIwQYMBaAFPY4eri7CuGrxh14YMTQe1qnBVjoMA8GA1UdEwEB/wQF
MAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIgPJmIjY1hoYRHjBMgLeV0x+wWietEyBfx
zyaulhhqnewCIQCmJ0kwBidqVzCOIx5H8CaEHUnTA/ULJGC2DDFzT7s54A==
-----END CERTIFICATE-----
EOF

# Upload root certificate to S3
aws --endpoint-url=http://localhost:4566 s3 cp root-certificate.pem s3://certificates/6bb42872-f4ed-4d55-a937-b8ffb8760de4/certificate.pem --region eu-west-2

cat <<EOF > document-signing-certificate.pem
-----BEGIN CERTIFICATE-----
MIIBtzCCAV2gAwIBAgIUZpfeB6WGkUsUk13SiJX8i6vG1IAwCgYIKoZIzj0EAwIw
QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
IExPQ0FMIGVudmlyb25tZW50MB4XDTI1MDkwMjEwMzMzMloXDTI2MDkwMjEwMzMz
MlowMjELMAkGA1UEBhMCR0IxIzAhBgNVBAMMGkV4YW1wbGUgSXNzdWVyIERTQyAo
TE9DQUwpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+NKi4QpYV/avqTFFoldR
IYEZaRgKF/qv+xJsek63Eh2cKn922zlJHj2KglzSlLm439BfFYGDYVet6W7pkvIY
fqNCMEAwHQYDVR0OBBYEFFBBWigj2hXjuJNidBxTFPvGxzOLMB8GA1UdIwQYMBaA
FPY4eri7CuGrxh14YMTQe1qnBVjoMAoGCCqGSM49BAMCA0gAMEUCIQCm99llHZfq
nPUS1X4/UZfbJ4HlbU33EaTqS/Y4vrOPVQIgLcG3k0jJQIxapcCUF7r/4rVUju0z
FmibH8pIONDZjSI=
-----END CERTIFICATE-----
EOF

# Upload document signing certificate to S3
aws --endpoint-url=http://localhost:4566 s3 cp document-signing-certificate.pem s3://certificates/1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4/certificate.pem --region eu-west-2
