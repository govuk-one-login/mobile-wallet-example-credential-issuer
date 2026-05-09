#!/bin/bash
set -eu

echo "Getting client public signing key"
aws kms get-public-key --endpoint-url=http://localhost:4561 --region eu-west-2 --key-id alias/localClientSigningKeyAlias --output text --query PublicKey | base64 --decode > LocalClientSigningKey.der
echo "Copying client public signing key to locally running auth stub"
openssl rsa -pubin -inform DER -outform PEM -in LocalClientSigningKey.der -pubout -out ../mobile-platform-back/auth-stub/src/client-keys/dev/exampleCri.pem
rm LocalClientSigningKey.der

echo "Updating locally running auth stub's environment variables to point to locally running Document Builder"
REGISTERED_REDIRECT_URIS='["http://localhost:8001/return-from-auth"]'
sed -i '' "s|^REGISTERED_REDIRECT_URIS=[^=]*$|REGISTERED_REDIRECT_URIS=${REGISTERED_REDIRECT_URIS}|" ../mobile-platform-back/auth-stub/.env

REGISTERED_POST_LOGOUT_REDIRECT_URIS='["http://localhost:8001/logged-out"]'
sed -i '' "s|^REGISTERED_POST_LOGOUT_REDIRECT_URIS=[^=]*$|REGISTERED_POST_LOGOUT_REDIRECT_URIS=${REGISTERED_POST_LOGOUT_REDIRECT_URIS}|" ../mobile-platform-back/auth-stub/.env

AUTH_STUB_BASE_URL="http://localhost:8000"
sed -i '' "s|^AUTH_STUB_BASE_URL=[^=]*$|AUTH_STUB_BASE_URL=${AUTH_STUB_BASE_URL}|" ../mobile-platform-back/auth-stub/.env

REDIRECT_URI="http://localhost:8001/return-from-auth"
sed -i '' "s|^REDIRECT_URI=[^=]*$|REDIRECT_URI=${REDIRECT_URI}|" ../mobile-platform-back/auth-stub/.env

AWS_PROFILE=$1
sed -i '' "s|^AWS_PROFILE=[^=]*$|AWS_PROFILE=${AWS_PROFILE}|" ../mobile-platform-back/auth-stub/.env
