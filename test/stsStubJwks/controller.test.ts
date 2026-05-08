process.env.STS_SIGNING_KEY_ID = "mock_signing_key_id";
process.env.ENVIRONMENT = "test";
import { stsStubJwksController } from "../../src/stsStubJwks/controller";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { GetPublicKeyCommand, KMSClient } from "@aws-sdk/client-kms";
import { mockClient } from "aws-sdk-client-mock";

const mockKmsClient = mockClient(KMSClient);

describe("controller.ts", () => {
  beforeEach(() => {
    mockKmsClient.reset();
  });

  it("should return 200 and the JWKs in the response body", async () => {
    const { res } = getMockRes();
    const req = getMockReq();
    mockKmsClient.on(GetPublicKeyCommand).resolves({
      KeyId:
        "arn:aws:kms:eu-west-2:000000000000:key/2ced22e2-c15b-4e02-aa5f-7a10a2eaccc7",
      PublicKey: Buffer.from(
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEam3RRiTF5JMowJUdkrpTG3SmgWBVCGHn2LQSHVrW31e7HMd9Y0avi01+L8shO3hPCeC1d/cw51giR100VD86Tw==",
        "base64",
      ),
    });

    await stsStubJwksController(req, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      keys: [
        {
          crv: "P-256",
          kid: "mock_signing_key_id",
          kty: "EC",
          x: "am3RRiTF5JMowJUdkrpTG3SmgWBVCGHn2LQSHVrW31c",
          y: "uxzHfWNGr4tNfi_LITt4TwngtXf3MOdYIkddNFQ_Ok8",
        },
      ],
    });
  });

  it("should return 500 if an unexpected error happens when fetching the public key from KMS", async () => {
    const { res } = getMockRes();
    const req = getMockReq();
    mockKmsClient.on(GetPublicKeyCommand).rejects(new Error("SOME_KMS_ERROR"));

    await stsStubJwksController(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: "server_error" });
  });
});
