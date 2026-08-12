process.env.ENVIRONMENT = "integration";
process.env.OIDC_CLIENT_ID = "client-id";
process.env.SELF = "http://localhost:3000";
process.env.CREDENTIAL_ISSUER_URL = "https://test-cri.example.com";
process.env.WALLET_APPS = "test-app-1";

import request from "supertest";
import { createApp } from "../../src/app";
import type { Application } from "express";

describe("Helmet CSP without OIDC endpoint", () => {
  let app: Application;

  beforeAll(async () => {
    app = await createApp();
  });

  it("should only include 'self' in form-action when no OIDC endpoint is configured", async () => {
    const response = await request(app).get("/healthcheck");

    expect(response.headers["content-security-policy"]).toContain(
      "form-action 'self'",
    );
    expect(response.headers["content-security-policy"]).not.toMatch(
      /form-action 'self' https?:\/\//,
    );
  });
});
