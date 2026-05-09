import { mockClient } from "aws-sdk-client-mock";
import {
  GetPublicKeyCommand,
  KMSClient,
  SignCommand,
} from "@aws-sdk/client-kms";
process.env.ENVIRONMENT = "test";
import { getProofJwt } from "../../src/proofJwt/proofJwt";
import format from "ecdsa-sig-formatter";

const mockKmsClient = mockClient(KMSClient);
const TIMESTAMP = 1756114156000;

describe("getProofJwt", () => {
  beforeEach(() => {
    mockKmsClient.reset();
    jest.spyOn(Date, "now").mockImplementation(() => TIMESTAMP);
  });

  afterEach(() => {
    jest.spyOn(Date, "now").mockRestore();
  });

  it("should return the proof JWT on success", async () => {
    mockKmsClient.on(GetPublicKeyCommand).resolves({
      PublicKey: Buffer.from(
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECO6A8rvNKD/sVNQwswdrIwR5ThN0gEc1rHtRzs5BXVvQ21bG1y7/b97RcxzbcQH+P2ti2DhwGiM/HwN5Agtg/Q==",
        "base64",
      ),
    });

    const mockSignature = Buffer.from(
      "yA4WNemRpUreSh9qgMh_ePGqhgn328ghJ_HG7WOBKQV98eFNm3FIvweoiSzHvl49Z6YTdV4Up7NDD7UcZ-52cw",
      "base64",
    );
    const mockSignatureDer = format.joseToDer(mockSignature, "ES256");
    mockKmsClient.on(SignCommand).resolves({ Signature: mockSignatureDer });

    const result = await getProofJwt(
      "test-nonce",
      "test-audience",
      "test-keyId",
    );

    const parts = result.split(".");
    expect(parts).toHaveLength(3);

    const decodedHeader = JSON.parse(
      Buffer.from(parts[0], "base64url").toString("utf8"),
    );
    expect(decodedHeader).toMatchObject({
      alg: "ES256",
      typ: "openid4vci-proof+jwt",
      kid: "did:key:zDnaeiG9cTwzu3BRBqvn9TW2nZqVaCFPZYkb8Ck4oxYujkkYr",
    });

    const decodedPayload = JSON.parse(
      Buffer.from(parts[1], "base64url").toString("utf8"),
    );
    expect(decodedPayload).toMatchObject({
      iss: "urn:fdc:gov:uk:wallet",
      aud: "test-audience",
      iat: Math.floor(TIMESTAMP / 1000),
      nonce: "test-nonce",
    });

    expect(parts[2]).toBe(mockSignature.toString("base64url"));
  });

  it("should throw error if KMS getPublicKey fails", async () => {
    mockKmsClient
      .on(GetPublicKeyCommand)
      .rejects(new Error("KMS public key error"));

    await expect(
      getProofJwt("test-nonce", "test-audience", "test-keyId"),
    ).rejects.toThrow("KMS public key error");
  });
});
