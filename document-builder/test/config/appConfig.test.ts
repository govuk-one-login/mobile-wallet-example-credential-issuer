import {
  getAwsRegion,
  getDocumentsTableName,
  getEnvironment,
  getCriEndpoint,
  getPortNumber,
  getSelfUrl,
  getOIDCClientId,
  getOIDCDiscoveryEndpoint,
  getClientSigningKeyId,
  getHardcodedWalletSubjectId,
  getWalletApps,
  getTableItemTtl,
  getPhotosBucketName,
  getOneLoginAuthServerUrl,
} from "../../src/config/appConfig";

describe("appConfig.ts", () => {
  it("should throw an error if PORT environment variable is not set", () => {
    expect(() => getPortNumber()).toThrow(
      new Error("PORT environment variable not set"),
    );
  });

  it("should return PORT environment variable value if set", () => {
    process.env.PORT = "8000";
    expect(getPortNumber()).toEqual("8000");
  });

  it("should throw an error if DOCUMENTS_TABLE_NAME environment variable is not set", () => {
    process.env.DOCUMENTS_TABLE_NAME = "";
    expect(() => getDocumentsTableName()).toThrow(
      new Error("DOCUMENTS_TABLE_NAME environment variable not set"),
    );
  });

  it("should return DOCUMENTS_TABLE_NAME environment variable value if set", () => {
    process.env.DOCUMENTS_TABLE_NAME = "testTable";
    expect(getDocumentsTableName()).toEqual("testTable");
  });

  it("should throw an error if PHOTOS_BUCKET_NAME environment variable is not set", () => {
    process.env.PHOTOS_BUCKET_NAME = "";
    expect(() => getPhotosBucketName()).toThrow(
      new Error("PHOTOS_BUCKET_NAME environment variable not set"),
    );
  });

  it("should return PHOTOS_BUCKET_NAME environment variable value if set", () => {
    process.env.PHOTOS_BUCKET_NAME = "testBucket";
    expect(getPhotosBucketName()).toEqual("testBucket");
  });

  it("should throw an error if ENVIRONMENT environment variable is not set", () => {
    delete process.env.ENVIRONMENT;
    expect(() => getEnvironment()).toThrow(
      new Error("ENVIRONMENT environment variable not set"),
    );
  });

  it("should return ENVIRONMENT environment variable value if set", () => {
    process.env.ENVIRONMENT = "local";
    expect(getEnvironment()).toEqual("local");
  });

  it("should throw an error if CREDENTIAL_ISSUER_URL environment variable is not set", () => {
    delete process.env.CREDENTIAL_ISSUER_URL;
    expect(() => getCriEndpoint()).toThrow(
      new Error("CREDENTIAL_ISSUER_URL environment variable not set"),
    );
  });

  it("should return CREDENTIAL_ISSUER_URL environment variable value if set", () => {
    process.env.CREDENTIAL_ISSUER_URL = "http://localhost:1234";
    expect(getCriEndpoint()).toEqual("http://localhost:1234");
  });

  it("should return the default value ('eu-west-2') of the AWS_REGION environment variable value if not set", () => {
    process.env.AWS_REGION = "";
    expect(getEnvironment()).toEqual("local");
  });

  it("should return AWS_REGION environment variable value if set", () => {
    process.env.AWS_REGION = "eu-west-3";
    expect(getAwsRegion()).toEqual("eu-west-3");
  });

  it("should throw an error if SELF environment variable is not set", () => {
    delete process.env.SELF;
    expect(() => getSelfUrl()).toThrow(
      new Error("SELF environment variable not set"),
    );
  });

  it("should return SELF environment variable value if set", () => {
    process.env.SELF = "test-url";
    expect(getSelfUrl()).toEqual("test-url");
  });

  it("should throw an error if OIDC_CLIENT_ID environment variable is not set", () => {
    delete process.env.OIDC_CLIENT_ID;
    expect(() => getOIDCClientId()).toThrow(
      new Error("OIDC_CLIENT_ID environment variable not set"),
    );
  });

  it("should return OIDC_CLIENT_ID environment variable value if set", () => {
    process.env.OIDC_CLIENT_ID = "test-client-id";
    expect(getOIDCClientId()).toEqual("test-client-id");
  });

  it("should throw an error if CLIENT_SIGNING_KEY_ID environment variable is not set", () => {
    delete process.env.CLIENT_SIGNING_KEY_ID;
    expect(() => getClientSigningKeyId()).toThrow(
      new Error("CLIENT_SIGNING_KEY_ID environment variable not set"),
    );
  });

  it("should return CLIENT_SIGNING_KEY_ID environment variable value if set", () => {
    process.env.CLIENT_SIGNING_KEY_ID = "test-client-key-id";
    expect(getClientSigningKeyId()).toEqual("test-client-key-id");
  });

  it("should throw an error if OIDC_ISSUER_DISCOVERY_ENDPOINT environment variable is not set in a non-integration environment", () => {
    delete process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT;
    process.env.ENVIRONMENT = "build";
    expect(() => getOIDCDiscoveryEndpoint()).toThrow(
      new Error("OIDC_ISSUER_DISCOVERY_ENDPOINT environment variable not set"),
    );
  });

  it("should return undefined if OIDC_ISSUER_DISCOVERY_ENDPOINT environment variable is not set in the integration environment", () => {
    delete process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT;
    process.env.ENVIRONMENT = "integration";
    expect(getOIDCDiscoveryEndpoint()).toBeUndefined();
  });

  it("should return undefined if OIDC_ISSUER_DISCOVERY_ENDPOINT environment variable is not set in the local environment", () => {
    delete process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT;
    process.env.ENVIRONMENT = "local";
    expect(getOIDCDiscoveryEndpoint()).toBeUndefined();
  });

  it("should return OIDC_ISSUER_DISCOVERY_ENDPOINT environment variable value if set", () => {
    process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT = "test-discovery-endpoint";
    expect(getOIDCDiscoveryEndpoint()).toEqual("test-discovery-endpoint");
  });

  it("should return wallet subject ID", () => {
    expect(getHardcodedWalletSubjectId()).toEqual(
      "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
    );
  });

  it("should throw an error if WALLET_APPS environment variable is not set", () => {
    delete process.env.WALLET_APPS;
    expect(() => getWalletApps()).toThrow(
      new Error("WALLET_APPS environment variable not set"),
    );
  });

  it("should return WALLET_APPS environment variable value as an array of strings if set", () => {
    process.env.WALLET_APPS = "test-app1,test-app2";
    expect(getWalletApps()).toEqual(["test-app1", "test-app2"]);
  });

  it("should return table item TTL", () => {
    expect(getTableItemTtl()).toEqual(43200);
  });

  it("should throw an error if ONE_LOGIN_AUTH_SERVER_URL environment variable is not set", () => {
    delete process.env.ONE_LOGIN_AUTH_SERVER_URL;
    expect(() => getOneLoginAuthServerUrl()).toThrow(
      new Error("ONE_LOGIN_AUTH_SERVER_URL environment variable not set"),
    );
  });

  it("should return ONE_LOGIN_AUTH_SERVER_URL environment variable value if set", () => {
    process.env.ONE_LOGIN_AUTH_SERVER_URL = "test-ol-auth-server-url";
    expect(getOneLoginAuthServerUrl()).toEqual("test-ol-auth-server-url");
  });
});
