import { getOIDCClient, getOIDCConfig } from "../../src/config/oidc";

process.env.OIDC_CLIENT_ID = "test-client-id";
process.env.OIDC_ISSUER_DISCOVERY_ENDPOINT = "http://localhost:8000";
process.env.SELF = "http://localhost:3000";

jest.mock("openid-client", () => ({
  Issuer: {
    discover: jest.fn(() => Promise.resolve({ Client: Object })),
  },
}));

describe("oidc.ts", () => {
  it("should return the OIDC configuration", () => {
    const response = getOIDCConfig();

    expect(response).toEqual({
      clientId: "test-client-id",
      discoveryEndpoint: "http://localhost:8000",
      redirectUri: "http://localhost:3000/return-from-auth",
    });
  });

  it("should return the OIDC client", async () => {
    const oidcConfig = getOIDCConfig();
    const response = await getOIDCClient(oidcConfig);

    expect(response).toEqual({
      client_id: "test-client-id",
      redirect_uris: ["http://localhost:3000/return-from-auth"],
      response_types: ["code"],
      token_endpoint_auth_method: "none",
      id_token_signed_response_alg: "ES256",
      scopes: "openid wallet-subject-id",
    });
  });
});
