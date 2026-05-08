#!/bin/sh

aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name documents \
    --attribute-definitions AttributeName=itemId,AttributeType=S \
    --key-schema AttributeName=itemId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --region eu-west-2

aws --endpoint-url=http://localhost:4566 dynamodb update-time-to-live --table-name documents \
                      --time-to-live-specification Enabled=true,AttributeName=timeToLive

aws --endpoint-url=http://localhost:4566 s3api create-bucket \
    --bucket photos \
    --region eu-west-2 \
    --create-bucket-configuration LocationConstraint=eu-west-2

aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec ECC_NIST_P256 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"2ced22e2-c15b-4e02-aa5f-7a10a2eaccc7"}]'

aws --endpoint-url=http://localhost:4566 kms create-alias \
    --region eu-west-2 \
    --alias-name alias/localStsSigningKeyAlias \
    --target-key-id 2ced22e2-c15b-4e02-aa5f-7a10a2eaccc7

aws --endpoint-url=http://localhost:4566 kms create-key \
    --region eu-west-2 \
    --key-usage SIGN_VERIFY \
    --key-spec RSA_2048 \
    --tags '[{"TagKey":"_custom_id_","TagValue":"14122ec4-cdd0-4154-8275-04363c15fbd9"}]'

aws --endpoint-url=http://localhost:4566 kms create-alias \
    --region eu-west-2 \
    --alias-name alias/localClientSigningKeyAlias \
    --target-key-id 14122ec4-cdd0-4154-8275-04363c15fbd9