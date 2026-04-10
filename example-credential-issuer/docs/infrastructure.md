# Example Credential Issuer Infrastructure

Mobile Wallet Example Credential Issuer runs as an ECS Fargate container in AWS eu-west-2.

```mermaid

flowchart LR
 subgraph ecs["ECS Fargate"]
        container["examplecri\gradlew\n Port 8000"]
  end
 subgraph vpc["VPC"]
        alb["ALB (internal)"]
        ecs
  end
 subgraph aws["AWS — eu-west-2"]
        dns["Route53"]
        agw["API Gateway V2\n ANY /{proxy+}"]
        vpc
        ecr[("ECR")]
        ddb1[("DynamoDB\n CredentialTable")]
        ddb2[("DynamoDB\n CredentialOfferTable")]
        kms1["KMS\n CredentialIssuerSigningKey"]
        kms2["KMS\n CRIDatabaseKey"]
  end
  subgraph dsc["Document Signing Certificate Issuer"]
        dscs3["DocSigningCerificate\n s3Bucket"]
  end
  subgraph platCA["Platform CA"]
        cert["Root CA Certificate"]
  end
    client(["Client"]) --> dns
    dns --> agw
    agw -- VPC Link --> alb
    alb --> container
    ecr -- image pull --> container
    container -- dynamodb:PutItem\n dynamodb:GetItem\n dynamodb:DeleteItem --> ddb1 & ddb2
    container -- kms:Sign\n kms:GetPublicKey --> kms1
    container <-- s3:GetObject --> dscs3
    cert -- s3:putObject  --> dscs3
    ddb1 -- kms:Decrypt\n kms:Encrypt --> kms2
    ddb2 -- kms:Decrypt\n kms:Encrypt --> kms2

```
