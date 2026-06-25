import { getEnvironment, getAwsRegion } from "./appConfig";
import { ENVIRONMENTS } from "./environments";
import { LocalAwsConfig } from "../types/LocalAwsConfig";
import { DynamoDBClientConfig } from "@aws-sdk/client-dynamodb";
import { KMSClientConfig } from "@aws-sdk/client-kms";
import { S3ClientConfig } from "@aws-sdk/client-s3";
import { logger } from "../middleware/logger";

const LOCAL_AWS_ENDPOINT = "http://localhost:4561";
const LOCAL_KMS_ENDPOINT = "http://localhost:4564";
const LOCAL_AWS_S3_ENDPOINT = "http://s3.localhost.localstack.cloud:4561"; // NOSONAR this is a local url used for testing

export function getLocalAwsConfig(endpoint: string): LocalAwsConfig {
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
  if (getEnvironment() === ENVIRONMENTS.LOCAL) {
    logger.info("Running database locally");
    return getLocalAwsConfig(LOCAL_AWS_ENDPOINT);
  }

  return {
    region: getAwsRegion(),
  };
}

export function getKmsConfig(): KMSClientConfig {
  if (getEnvironment() === ENVIRONMENTS.LOCAL) {
    logger.info("Running KMS locally");
    return {
      endpoint: LOCAL_KMS_ENDPOINT,
    };
  }

  return {
    region: getAwsRegion(),
  };
}

export function getS3Config(): S3ClientConfig {
  if (getEnvironment() === ENVIRONMENTS.LOCAL) {
    logger.info("Running S3 locally");
    return getLocalAwsConfig(LOCAL_AWS_S3_ENDPOINT);
  }

  return {
    region: getAwsRegion(),
  };
}
