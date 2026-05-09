process.env.ENVIRONMENT = "test";
process.env.OIDC_CLIENT_ID = "client-id";
process.env.OIDC_PRIVATE_KEY = "private-key";
process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT = "discovery-endpoint";
process.env.SELF = "redirect-uri";
process.env.CREDENTIAL_ISSUER_URL = "https://test-cri.example.com";
process.env.WALLET_APPS = "test-app-1,test-app-2";

import { createApp } from "../src/app";
import { expect } from "@jest/globals";

describe("app.ts", () => {
  it("should successfully create an app", async () => {
    expect(async () => await createApp()).not.toThrow();
  });
});
