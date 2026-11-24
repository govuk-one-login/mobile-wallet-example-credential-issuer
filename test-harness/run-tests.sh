#!/bin/bash

# Entrypoint of the test image. It is installed at /run-tests.sh, which is where the secure pipeline expects it.
# The pipeline provides the deployed stack outputs as CFN_* environment variables, plus TEST_ENVIRONMENT
# and TEST_REPORT_ABSOLUTE_DIR.
# Without TEST_REPORT_ABSOLUTE_DIR (local runs and GitHub Actions) the original flow is used.

set -eu

cd /workspace

if [[ -z "${TEST_REPORT_ABSOLUTE_DIR:-}" ]]; then
  exec bash run-server-and-tests.sh
fi

remove_quotes() {
  echo "$1" | tr -d '"'
}

CRI_HOST=$(remove_quotes "${CFN_CredIssuerFriendlyUrl:?CFN_CredIssuerFriendlyUrl is not set}")
export CRI_URL="https://${CRI_HOST}"
export WALLET_SUBJECT_ID="urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i"
export HAS_NOTIFICATION_ENDPOINT="true"

# TODO: needs a stack output (or another source) that provides the OIDC client id
CLIENT_ID=$(remove_quotes "${CFN_ClientId:?CFN_ClientId is not set}")
export CLIENT_ID

# TODO: set the itemIds of the pre-seeded documents for each environment
ITEM_ID_JWT=""
ITEM_ID_MDOC=""

if [[ "$TEST_ENVIRONMENT" == "dev" ]] || [[ "$TEST_ENVIRONMENT" == "build" ]]; then
  export TEST_HARNESS_URL="https://sts-mock.wallet-onboarding.${TEST_ENVIRONMENT}.account.gov.uk"
  # Signature verification is disabled in these environments, so the negative tests would
  # redeem the credential offer and make the happy path tests fail.
  export SKIP_NEGATIVE_TESTS=true
else
  echo "Test harness is not configured to run in the ${TEST_ENVIRONMENT} environment" >&2
  exit 1
fi

if [[ -z "$ITEM_ID_JWT" ]] || [[ -z "$ITEM_ID_MDOC" ]]; then
  echo "The itemIds of the pre-seeded documents are not set" >&2
  exit 1
fi

run_tests() {
  local format="$1"
  local credential_type="$2"
  local item_id="$3"
  local offer
  local exit_code=0

  offer=$(curl --fail --silent --show-error --get "${CRI_URL}/credential_offer" \
    --data-urlencode "walletSubjectId=${WALLET_SUBJECT_ID}" \
    --data-urlencode "itemId=${item_id}" \
    --data-urlencode "credentialType=${credential_type}") || {
    echo "Failed to get a credential offer for ${credential_type}" >&2
    return 1
  }

  CREDENTIAL_FORMAT="$format" CREDENTIAL_OFFER_DEEP_LINK="$(remove_quotes "$offer")" npm run test || exit_code=$?

  cp -f results/report.xml "${TEST_REPORT_ABSOLUTE_DIR}/report-${format}.xml" || true
  return $exit_code
}

bash run-server.sh &
SERVER_PID=$!
sleep 5

EXIT_CODE=0
run_tests jwt DigitalVeteranCard "$ITEM_ID_JWT" || EXIT_CODE=1
run_tests mdoc org.iso.18013.5.1.mDL "$ITEM_ID_MDOC" || EXIT_CODE=1

kill $SERVER_PID
exit $EXIT_CODE
