# Status List Mock

## Introduction

The template.yaml in this project deploys the following AWS resources:

- a Lambda function to issue status list entries
- a Lambda function to revoke status list entries
- a Lambda function that uploads a JWKS to S3
- an S3 bucket to store the status list
- an S3 bucket to store the JWKS

## Deploy

### Deploy with the AWS SAM CLI

Before deploying with the AWS SAM CLI, you must authenticate with AWS. Once authenticated, run the following commands:

1. Build the application:

```bash
sam build
```

2. Deploy to AWS:

```bash
sam deploy --guided --capabilities CAPABILITY_IAM --stack-name <your_stack_name>
```