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
