import { getEnvironment, getAwsRegion } from "./appConfig";
import { LocalStackAwsConfig } from "../types/LocalStackAwsConfig";
import { DynamoDBClientConfig } from "@aws-sdk/client-dynamodb";
import { KMSClientConfig } from "@aws-sdk/client-kms";
import { S3ClientConfig } from "@aws-sdk/client-s3";
import { logger } from "../middleware/logger";

const LOCALSTACK_ENDPOINT = "http://localhost:4561";
const LOCALSTACK_S3_ENDPOINT = "http://s3.localhost.localstack.cloud:4561";

export function getLocalStackAwsConfig(endpoint: string): LocalStackAwsConfig {
  return {
    endpoint: endpoint,
    credentials: {
      accessKeyId: "accessKeyId",
      secretAccessKey: "secretAccessKey",
    },
    region: getAwsRegion(),
  };
}

export function getDatabaseConfig(): DynamoDBClientConfig {
  if (getEnvironment() === "local") {
    logger.info("Running database locally");
    return getLocalStackAwsConfig(LOCALSTACK_ENDPOINT);
  }

  return {
    region: getAwsRegion(),
  };
}

export function getKmsConfig(): KMSClientConfig {
  if (getEnvironment() === "local") {
    logger.info("Running KMS locally");
    return getLocalStackAwsConfig(LOCALSTACK_ENDPOINT);
  }

  return {
    region: getAwsRegion(),
  };
}

export function getS3Config(): S3ClientConfig {
  if (getEnvironment() === "local") {
    logger.info("Running S3 locally");
    return getLocalStackAwsConfig(LOCALSTACK_S3_ENDPOINT);
  }

  return {
    region: getAwsRegion(),
  };
}
