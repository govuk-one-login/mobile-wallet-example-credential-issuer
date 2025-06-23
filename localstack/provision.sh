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

aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4"}]'

aws --endpoint-url=http://localhost:4566 kms create-alias \
    --region eu-west-2 \
    --alias-name alias/documentSigningKeyAlias1 \
    --target-key-id 1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4