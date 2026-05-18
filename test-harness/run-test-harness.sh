#!/bin/bash

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOUR='\033[0m' # No Color

# Logging functions
log_info() {
  local msg="$1"
  echo -e "${GREEN}[INFO]${NO_COLOUR} ${msg}"
  return 0
}

log_error() {
  local msg="$1"
  echo -e "${RED}[ERROR]${NO_COLOUR} ${msg}" >&2
  return 0
}

# Usage function
show_usage() {
  cat << EOF
Usage: $0 <CREDENTIAL_FORMAT> <CREDENTIAL_OFFER_DEEP_LINK> [OPTIONS]

Required arguments:
  CREDENTIAL_FORMAT          Either 'jwt' or 'mdoc'
  CREDENTIAL_OFFER_DEEP_LINK Deep link for the credential offer

Optional arguments:
  --container-name NAME      Docker container name (default: test-harness)
  --network-name NAME        Docker network name (default: bridge)
  --test-harness-url URL     Test harness URL (auto-derived if not set)
  --cri-url URL              CRI URL (default: http://localhost:8080)
  --has-notification-endpoint BOOL  Whether CRI has notification endpoint (default: true)
  --wallet-subject-id ID     Wallet subject ID (default: test ID)
  --client-id ID             Client ID (default: TEST_CLIENT_ID)
  --help, -h                 Show this help message

EOF
  return 0
}

# Validate arguments
if [[ "$#" -lt 2 ]]; then
  log_error "Missing required arguments"
  show_usage
  exit 1
fi

# Handle help flag
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
  show_usage
  exit 0
fi

CREDENTIAL_FORMAT="$1"
CREDENTIAL_OFFER_DEEP_LINK="$2"
shift 2

# Validate credential format
if [[ "$CREDENTIAL_FORMAT" != "jwt" && "$CREDENTIAL_FORMAT" != "mdoc" ]]; then
  log_error "Credential format must be either 'jwt' or 'mdoc', got: $CREDENTIAL_FORMAT"
  show_usage
  exit 1
fi

# Validate deep link is present
if [[ -z "$CREDENTIAL_OFFER_DEEP_LINK" ]]; then
  log_error "Credential offer deep link cannot be empty"
  show_usage
  exit 1
fi

# Default values
CONTAINER_NAME="test-harness"
NETWORK_NAME="bridge"
TEST_HARNESS_URL=""
CRI_URL="http://localhost:8080"
HAS_NOTIFICATION_ENDPOINT="true"
WALLET_SUBJECT_ID="urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i"
CLIENT_ID="TEST_CLIENT_ID"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --container-name)
      [[ -z "${2:-}" ]] && { log_error "--container-name requires a value"; show_usage; exit 1; }
      CONTAINER_NAME="$2"
      shift 2
      ;;
    --network-name)
      [[ -z "${2:-}" ]] && { log_error "--network-name requires a value"; show_usage; exit 1; }
      NETWORK_NAME="$2"
      shift 2
      ;;
    --test-harness-url)
      [[ -z "${2:-}" ]] && { log_error "--test-harness-url requires a value"; show_usage; exit 1; }
      TEST_HARNESS_URL="$2"
      shift 2
      ;;
    --cri-url)
      [[ -z "${2:-}" ]] && { log_error "--cri-url requires a value"; show_usage; exit 1; }
      CRI_URL="$2"
      shift 2
      ;;
    --has-notification-endpoint)
      [[ -z "${2:-}" ]] && { log_error "--has-notification-endpoint requires a value"; show_usage; exit 1; }
      if [[ "$2" != "true" && "$2" != "false" ]]; then
        log_error "--has-notification-endpoint must be 'true' or 'false'"
        show_usage
        exit 1
      fi
      HAS_NOTIFICATION_ENDPOINT="$2"
      shift 2
      ;;
    --wallet-subject-id)
      [[ -z "${2:-}" ]] && { log_error "--wallet-subject-id requires a value"; show_usage; exit 1; }
      WALLET_SUBJECT_ID="$2"
      shift 2
      ;;
    --client-id)
      [[ -z "${2:-}" ]] && { log_error "--client-id requires a value"; show_usage; exit 1; }
      CLIENT_ID="$2"
      shift 2
      ;;
    *)
      log_error "Unknown option: $1"
      show_usage
      exit 1
      ;;
  esac
done

# Auto-derive TEST_HARNESS_URL if not provided
if [[ -z "$TEST_HARNESS_URL" ]]; then
  if [[ "$NETWORK_NAME" == "bridge" ]]; then
    TEST_HARNESS_URL="http://localhost:3001"
  else
    TEST_HARNESS_URL="http://$CONTAINER_NAME:3001"
  fi
  log_info "Auto-derived TEST_HARNESS_URL: $TEST_HARNESS_URL"
fi

# Display configuration
log_info "Test harness configuration:"
cat << EOF
  Container Name: $CONTAINER_NAME
  Network Name: $NETWORK_NAME
  Test Harness URL: $TEST_HARNESS_URL
  Credential Format: $CREDENTIAL_FORMAT
  Credential Offer: $CREDENTIAL_OFFER_DEEP_LINK
  CRI URL: $CRI_URL
  Has Notification Endpoint: $HAS_NOTIFICATION_ENDPOINT
  Wallet Subject ID: $WALLET_SUBJECT_ID
  Client ID: $CLIENT_ID
EOF

# Build Docker image
log_info "Building Docker image..."
if ERROR=$(docker build -t "test-harness" . 2>&1); then
  log_info "Docker image built successfully"
else
  log_error "Docker build failed:"
  echo "$ERROR" >&2
  exit 1
fi

# Run container
log_info "Starting test harness container..."
docker run --rm \
  -v "$(pwd)/output:/workspace/results" \
  -p 3001:3001 \
  --name "$CONTAINER_NAME" \
  --network "$NETWORK_NAME" \
  -e TEST_HARNESS_URL="$TEST_HARNESS_URL" \
  -e CREDENTIAL_OFFER_DEEP_LINK="$CREDENTIAL_OFFER_DEEP_LINK" \
  -e CREDENTIAL_FORMAT="$CREDENTIAL_FORMAT" \
  -e CRI_URL="$CRI_URL" \
  -e HAS_NOTIFICATION_ENDPOINT="$HAS_NOTIFICATION_ENDPOINT" \
  -e WALLET_SUBJECT_ID="$WALLET_SUBJECT_ID" \
  -e CLIENT_ID="$CLIENT_ID" \
  test-harness

log_info "Test harness completed. Check ./output/ for results."
