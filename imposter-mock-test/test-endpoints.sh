#!/bin/bash

API_URL="http://localhost:9090"

echo "Testing JWKS Endpoint"
curl -i "${API_URL}/.well-known/jwks.json"

echo -e "\nTesting Authorize Endpoint"
curl -i -L "${API_URL}/authorize?client_id=test&redirect_uri=https://example.com&state=xyz&response_type=code&nonce=123&code_challenge=abc&code_challenge_method=S256"

echo -e "\nTesting Redirect Endpoint"
curl -i -L "${API_URL}/redirect?state=91446467-5127-4af4-a0ec-48e4b4347820&code=wMVoGvE7AY"

echo -e "\nTesting Token Endpoint - Authorization Code Flow"
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=TYy4jA3fyj" \
  -d "redirect_uri=https://example.com/callback" \

echo -e "\nTesting Token Endpoint - Pre-authorized Code Flow"
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code" \
  -d "pre-authorized_code=eyJraWQiOiI3OGZhMTMxZDY3N2MxYWMwZjE3MmM1M2I0N2FjMTY5YTk1YWQwZDkyYzM4YmQ3OTRhNzBkYTU5MDMyMDU4Mjc0IiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjbGllbnRJZCI6IlRFU1RfQ0xJRU5UX0lEIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiY3JlZGVudGlhbF9pZGVudGlmaWVycyI6WyJlZmJiNjU3YS02ZDBlLTQ0YjctOGU4ZS0zZDE0MjRhYTU5NDgiXSwiZXhwIjoxNzcwNjUyNjM4LCJpYXQiOjE3NzA2NTE3Mzh9.hIXERpzUGZ1QlPg80YjCUNebDFeYKVbhF0xQ6JAeWtbT0qVUOMsjZIhmEWJmGypwminrznx7Iv4AJHAPVXktcg" \
  -d "client_id=yDaaZKL33G"

echo -e "\nTesting Token Endpoint - Token Exchange Flow"
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
  -d "scope=Q8EpBx6MtC" \
  -d "subject_token=vu17RX3zQs" \
  -d "subject_token_type=urn:ietf:params:oauth:token-type:access_token" \

echo -e "\nTesting Token Endpoint - Refresh Token Flow"
curl -i -X POST "${API_URL}/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=vN0TdvRLI5"

