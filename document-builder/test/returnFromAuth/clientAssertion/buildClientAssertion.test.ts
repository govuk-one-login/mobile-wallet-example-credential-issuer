import { buildClientAssertion } from "../../../src/returnFromAuth/clientAssertion/buildClientAssertion";
import { KmsService } from "../../../src/services/kmsService";
import * as jose from "jose";

describe("buildClientAssertion.ts", () => {
  jest.useFakeTimers().setSystemTime(new Date("2024-06-19"));

  it("should return the JWT access token", async () => {
    const mockKmsService = {
      sign: jest.fn(() => Promise.resolve("mocked_signature")),
    };

    const response = await buildClientAssertion(
      "test_client_id",
      "http://localost:8000/token",
      "mock_key_id",
      mockKmsService as unknown as KmsService,
    );
    const header = jose.decodeProtectedHeader(response);
    const claims = jose.decodeJwt(response);

    expect(response).toBeDefined();
    expect(header).toEqual({ alg: "RS512", typ: "JWT" });
    expect(claims).toHaveProperty("sub", "test_client_id");
    expect(claims).toHaveProperty("iss", "test_client_id");
    expect(claims).toHaveProperty("aud", "http://localost:8000/token");
    expect(claims).toHaveProperty("iat", 1718755200);
    expect(claims).toHaveProperty("exp", 1718755500);
    expect(claims).toHaveProperty("jti");
  });
});
