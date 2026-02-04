# STS Imposter Mock Test

A mock of STS built using Imposter mock server.

## Prerequisites

- Docker

## Files

- `openapi.yaml` - STS OpenAPI specification
- `imposter-config.yaml` - Imposter configuration mapping endpoints to scripts
- `authorize.groovy` - Script handling OAuth authorization requests
- `redirect.groovy` - Script handling redirect callbacks
- `token.groovy` - Script handling token flows (authorization code, token exchange, pre-authorized code, refresh token)
- `Dockerfile` - Container image definition
- `test-endpoints.sh` - Script to test all endpoints locally

## Running Locally

### 1. Build the Docker image

```bash
docker build -t sts-mock .
```

### 2. Run the container

```bash
docker run -p 9090:8080 -e ISSUER_URL=http://host.docker.internal:8080 -e SELF_URL=http://localhost:9090 -e WALLET_REDIRECT_URI=https://example.com/callback sts-mock
```

### 3. Test the endpoints

Run the test script:

```bash
chmod +x test-endpoints.sh
./test-endpoints.sh
```
