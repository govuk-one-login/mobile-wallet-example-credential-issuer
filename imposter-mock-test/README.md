# Imposter Mock Test

Deploy a mock of STS built using Imposter mock server to AWS ECS using CloudFormation.

## Prerequisites

- Docker
- AWS CLI
- AWS SAM CLI
- AWS account with appropriate permissions

## Files

- `openapi.yaml` - Dummy OpenAPI specification
- `imposter-config.yaml` - Imposter configuration
- `Dockerfile` - Container image definition
- `template.yaml` - CloudFormation template for ECS deployment
- `build-and-deploy-image.sh` - Script to build and push Docker image

## Deployment

### 1. Build and push Docker image

```bash
chmod +x build-and-deploy-image.sh
./build-and-deploy-image.sh <image-tag> <aws-profile>
```

Example:
```bash
./build-and-deploy-image.sh v1.0.0 my-aws-profile
```

### 2. Deploy CloudFormation stack

```bash
sam build && sam deploy --capabilities CAPABILITY_IAM --stack-name my-stack-name
```

## Testing

Once deployed, the mock server will be available via API Gateway (check CloudFormation outputs):

```bash
curl https://<api-id>.execute-api.eu-west-2.amazonaws.com/health
```
