#!/bin/bash

# Set your API Gateway URL here
API_URL="https://your-api-id.execute-api.eu-west-2.amazonaws.com"

echo "=== Testing JWKS Endpoint ==="
curl -i "${API_URL}/.well-known/jwks.json"

echo -e "\n\n=== Testing Authorize Endpoint ==="
curl -i "${API_URL}/authorize?client_id=test-client&redirect_uri=https://example.com/callback&state=abc123&response_type=code"

echo -e "\n\n=== Testing Redirect Endpoint ==="
curl -i "${API_URL}/redirect?redirect_uri=https://example.com/callback&state=xyz789"

echo -e "\n\n=== Testing Token Endpoint - Pre-authorized Code Flow ==="
PRE_AUTH_CODE="eyJhbGciOiJFUzI1NiIsInR5cCI6ImF0K2p3dCIsImtpZCI6Im1vY2sta2V5LTEifQ.eyJzdWIiOiJ1c2VyMTIzIiwiaXNzIjoiaHR0cHM6Ly9pc3N1ZXIuZXhhbXBsZS5jb20iLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImNyZWRlbnRpYWwtMSJdfQ.signature"
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code" \
  -d "pre-authorized_code=${PRE_AUTH_CODE}" \
  -d "client_id=test-client"

echo -e "\n\n=== Testing Token Endpoint - Authorization Code Flow ==="
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=mock_auth_code_abc123" \
  -d "redirect_uri=https://example.com/callback" \
  -d "client_id=test-client"

echo -e "\n\n=== Testing Token Endpoint - Token Exchange Flow ==="
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
  -d "subject_token=mock_access_token_123" \
  -d "subject_token_type=urn:ietf:params:oauth:token-type:access_token" \
  -d "resource=https://service.example.com"

echo -e "\n\n=== Testing Token Endpoint - Refresh Token Flow ==="
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=mock_refresh_token_xyz789"
