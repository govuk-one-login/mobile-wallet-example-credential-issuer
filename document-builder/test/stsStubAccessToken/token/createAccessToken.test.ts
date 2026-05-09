import { createAccessToken } from "../../../src/stsStubAccessToken/token/createAccessToken";
import { KmsService } from "../../../src/services/kmsService";
import crypto from "crypto";

const MOCK_C_NONCE = "1a0fac05-4b38-480f-9cbd-b046eabe1e22";
const MOCK_JTI = "e7b53511-a780-4634-8153-763dcbf6b519";
const TIMESTAMP = 1756114156000;

describe("createAccessToken", () => {
  beforeEach(() => {
    jest.spyOn(Date, "now").mockImplementation(() => TIMESTAMP);
    jest
      .spyOn(crypto, "randomUUID")
      .mockImplementationOnce(() => MOCK_C_NONCE)
      .mockImplementationOnce(() => MOCK_JTI);
  });

  afterEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Date, "now").mockRestore();
    jest.spyOn(crypto, "randomUUID").mockRestore();
  });

  it("should return the access token", async () => {
    const mockKmsService = {
      sign: jest.fn(() => Promise.resolve("mocked_signature")),
    };

    const response = await createAccessToken(
      "mock_wallet_subject_id",
      {
        iss: "mock_issuer",
        aud: "mock_audience",
        credential_identifiers: ["mock_credential_identifier"],
      },
      "mock_key_id",
      180,
      mockKmsService as unknown as KmsService,
    );

    const [encodedHeader, encodedPayload, signature] = response.split(".");
    const header = JSON.parse(atob(encodedHeader));
    const payload = JSON.parse(atob(encodedPayload));
    expect(header).toEqual({
      alg: "ES256",
      typ: "at+jwt",
      kid: "mock_key_id",
    });
    expect(payload).toEqual({
      sub: "mock_wallet_subject_id",
      iss: "mock_audience",
      aud: "mock_issuer",
      credential_identifiers: ["mock_credential_identifier"],
      c_nonce: MOCK_C_NONCE,
      exp: Math.floor(TIMESTAMP / 1000) + 180,
      jti: MOCK_JTI,
    });

    expect(signature).toBe("mocked_signature");
  });
});
