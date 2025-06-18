#!/bin/sh

export TABLE_NAME=credential_offer_cache

aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name $TABLE_NAME \
    --attribute-definitions AttributeName=credentialIdentifier,AttributeType=S \
    --key-schema AttributeName=credentialIdentifier,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --region eu-west-2

aws --endpoint-url=http://localhost:4566 dynamodb update-time-to-live --table-name $TABLE_NAME \
                      --time-to-live-specification Enabled=true,AttributeName=timeToLive

aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"ff275b92-0def-4dfc-b0f6-87c96b26c6c7"}]'

aws --endpoint-url=http://localhost:4566 kms create-alias \
    --region eu-west-2 \
    --alias-name alias/localSigningKeyAlias \
    --target-key-id ff275b92-0def-4dfc-b0f6-87c96b26c6c7

aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket certificates --create-bucket-configuration LocationConstraint=eu-west-2 --region eu-west-2

cat <<EOF > certificate.pem
-----BEGIN CERTIFICATE-----
MIIB8TCCAZegAwIBAgIQLmndcaaE19g+3lJupyQCojAKBggqhkjOPQQDAjA/MQsw
CQYDVQQGEwJVSzEwMC4GA1UEAwwnbURMIEV4YW1wbGUgSUFDQSBSb290IC0gREVW
IGVudmlyb25tZW50MB4XDTI1MDQxNTA5MTQyMFoXDTM0MDQxNjEwMTQyMFowPzEL
MAkGA1UEBhMCVUsxMDAuBgNVBAMMJ21ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIERF
ViBlbnZpcm9ubWVudDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBV+UF7RuBa4
gu0aVFRRD1plr+Bnu1dsv9eNXbU2ZqGq0FkM4IknCZ12Y/zENDVA8VyM+hNRlkvr
SZMoqCSnobejdTBzMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFHomsAv2
hf6lUS/4necL1PqJLNEJMA8GA1UdDwEB/wQFAwMHBgAwLQYDVR0SBCYwJIYiaHR0
cHM6Ly9tb2JpbGUuZGV2LmFjY291bnQuZ292LnVrLzAKBggqhkjOPQQDAgNIADBF
AiBFqaelXoq3kySjLkoy6cbnv5mFfUjyFN9emgHyWcy2OgIhAOtizfGVyHNAQ2wD
z6mnTX/lWqYiEThH9Gb3xRXKrslN
-----END CERTIFICATE-----
EOF

aws --endpoint-url=http://localhost:4566 s3 cp certificate.pem s3://certificates/6bb42872-f4ed-4d55-a937-b8ffb8760de4/ --region eu-west-2
