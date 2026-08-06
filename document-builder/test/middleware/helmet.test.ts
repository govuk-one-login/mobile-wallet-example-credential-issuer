process.env.ENVIRONMENT = "test";
process.env.OIDC_CLIENT_ID = "client-id";
process.env.OIDC_PRIVATE_KEY = "private-key";
process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT = "discovery-endpoint";
process.env.SELF = "http://localhost:3000";
process.env.CREDENTIAL_ISSUER_URL = "https://test-cri.example.com";
process.env.WALLET_APPS = "test-app-1,test-app-2";

import request from "supertest";
import { createApp } from "../../src/app";
import type { Application } from "express";

describe("Helmet security headers", () => {
  let app: Application;

  beforeAll(async () => {
    app = await createApp();
  });

  it("should set Content-Security-Policy header", async () => {
    const response = await request(app).get("/healthcheck");

    expect(response.headers["content-security-policy"]).toBeDefined();
    expect(response.headers["content-security-policy"]).toContain(
      "default-src 'self'",
    );
    expect(response.headers["content-security-policy"]).toContain(
      "script-src 'self' 'unsafe-inline'",
    );
    expect(response.headers["content-security-policy"]).toContain(
      "style-src 'self' 'unsafe-inline'",
    );
    expect(response.headers["content-security-policy"]).toContain(
      "img-src 'self' data:",
    );
  });

  it("should set X-Content-Type-Options header", async () => {
    const response = await request(app).get("/healthcheck");

    expect(response.headers["x-content-type-options"]).toBe("nosniff");
  });

  it("should set Strict-Transport-Security header", async () => {
    const response = await request(app).get("/healthcheck");

    expect(response.headers["strict-transport-security"]).toBeDefined();
    expect(response.headers["strict-transport-security"]).toContain(
      "max-age=",
    );
  });

  it("should set X-Frame-Options header", async () => {
    const response = await request(app).get("/healthcheck");

    expect(response.headers["x-frame-options"]).toBe("SAMEORIGIN");
  });

  it("should not expose X-Powered-By header", async () => {
    const response = await request(app).get("/healthcheck");

    expect(response.headers["x-powered-by"]).toBeUndefined();
  });
});
