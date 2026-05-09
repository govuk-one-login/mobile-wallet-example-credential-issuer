import {
  getLocalStackAwsConfig,
  getDatabaseConfig,
  getKmsConfig,
  getS3Config,
} from "../../src/config/aws";

describe("aws.ts", () => {
  it("should return the LocalStack AWS configuration", () => {
    expect(getLocalStackAwsConfig("http://localhost:test")).toStrictEqual({
      credentials: {
        accessKeyId: "accessKeyId",
        secretAccessKey: "secretAccessKey",
      },
      endpoint: "http://localhost:test",
      region: "eu-west-2",
    });
  });

  it("should return the database configuration for the 'local' environment", () => {
    process.env.ENVIRONMENT = "local";
    expect(getDatabaseConfig()).toStrictEqual({
      credentials: {
        accessKeyId: "accessKeyId",
        secretAccessKey: "secretAccessKey",
      },
      endpoint: "http://localhost:4561",
      region: "eu-west-2",
    });
  });

  it("should return the database configuration for the 'development' environment", () => {
    process.env.ENVIRONMENT = "development";
    expect(getDatabaseConfig()).toStrictEqual({ region: "eu-west-2" });
  });

  it("should return the KMS configuration for the 'local' environment", () => {
    process.env.ENVIRONMENT = "local";
    expect(getKmsConfig()).toStrictEqual({
      credentials: {
        accessKeyId: "accessKeyId",
        secretAccessKey: "secretAccessKey",
      },
      endpoint: "http://localhost:4561",
      region: "eu-west-2",
    });
  });

  it("should return the KMS configuration for the 'development' environment", () => {
    process.env.ENVIRONMENT = "development";
    expect(getKmsConfig()).toStrictEqual({ region: "eu-west-2" });
  });

  it("should return the S3 configuration for the 'local' environment", () => {
    process.env.ENVIRONMENT = "local";
    expect(getS3Config()).toStrictEqual({
      credentials: {
        accessKeyId: "accessKeyId",
        secretAccessKey: "secretAccessKey",
      },
      endpoint: "http://s3.localhost.localstack.cloud:4561",
      region: "eu-west-2",
    });
  });

  it("should return the S3 configuration for the 'development' environment", () => {
    process.env.ENVIRONMENT = "development";
    expect(getS3Config()).toStrictEqual({ region: "eu-west-2" });
  });
});
