import { mockClient } from "aws-sdk-client-mock";
import {
  GetPublicKeyCommand,
  KMSClient,
  SignCommand,
} from "@aws-sdk/client-kms";
import { KmsService } from "../../src/services/kmsService";
import format from "ecdsa-sig-formatter";

const mockKmsClient = mockClient(KMSClient);
const SIGNING_ALGORITHM_RSA = "RSASSA_PKCS1_V1_5_SHA_256";
const SIGNING_ALGORITHM_EC = "ECDSA_SHA_256";

let kmsService: KmsService;

describe("kmsService.ts", () => {
  beforeEach(() => {
    kmsService = new KmsService("mock_key_id", new KMSClient());
    mockKmsClient.reset();
  });

  it("should throw an error when KMS response has no signature", async () => {
    mockKmsClient.on(SignCommand).resolves({ Signature: undefined });

    await expect(
      kmsService.sign("mock_message_to_sign", SIGNING_ALGORITHM_RSA),
    ).rejects.toThrow("No signature returned");
  });

  it("should decode DER signature when signing algorithm is EC and should return a base64 encoded signature", async () => {
    const mockSignature = Buffer.from(
      "yA4WNemRpUreSh9qgMh_ePGqhgn328ghJ_HG7WOBKQV98eFNm3FIvweoiSzHvl49Z6YTdV4Up7NDD7UcZ-52cw",
      "base64",
    );
    const mockSignatureDer = format.joseToDer(mockSignature, "ES256");
    mockKmsClient.on(SignCommand).resolves({ Signature: mockSignatureDer });
    const response = await kmsService.sign(
      "mock_message_to_sign",
      SIGNING_ALGORITHM_EC,
    );

    expect(response).toEqual(
      "yA4WNemRpUreSh9qgMh_ePGqhgn328ghJ_HG7WOBKQV98eFNm3FIvweoiSzHvl49Z6YTdV4Up7NDD7UcZ-52cw",
    );
  });

  it("should NOT decode DER signature when signing algorithm is RSA and should return a base64 encoded signature", async () => {
    const mockSignature = Buffer.from(
      "yA4WNemRpUreSh9qgMh_ePGqhgn328ghJ_HG7WOBKQV98eFNm3FIvweoiSzHvl49Z6YTdV4Up7NDD7UcZ-52cw",
      "base64",
    );
    mockKmsClient.on(SignCommand).resolves({ Signature: mockSignature });
    const response = await kmsService.sign(
      "mock_message_to_sign",
      SIGNING_ALGORITHM_RSA,
    );

    expect(response).toEqual(
      "yA4WNemRpUreSh9qgMh_ePGqhgn328ghJ_HG7WOBKQV98eFNm3FIvweoiSzHvl49Z6YTdV4Up7NDD7UcZ-52cw",
    );
  });

  it("should throw an error when KMS response has no public key", async () => {
    mockKmsClient.on(GetPublicKeyCommand).resolves({
      KeyId:
        "arn:aws:kms:eu-west-2:000000000000:key/2ced22e2-c15b-4e02-aa5f-7a10a2eaccc7",
      PublicKey: undefined,
      KeySpec: "RSA_4096",
      KeyUsage: "SIGN_VERIFY",
      SigningAlgorithms: ["RSASSA_PKCS1_V1_5_SHA_256"],
    });

    await expect(kmsService.getPublicKey()).rejects.toThrow(
      "No public key returned",
    );
  });

  it("should return a base64 encoded public key when call to KMS is successful", async () => {
    mockKmsClient.on(GetPublicKeyCommand).resolves({
      KeyId:
        "arn:aws:kms:eu-west-2:000000000000:key/2ced22e2-c15b-4e02-aa5f-7a10a2eaccc7",
      PublicKey: Buffer.from(
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAuA1gxsWNOVSboz38+wAAeqKjq+yudtNpfg+xUuLKDLp+KcvYU84oSlxe1h4cCAwEAAQ==",
        "base64",
      ),
      KeySpec: "RSA_4096",
      KeyUsage: "SIGN_VERIFY",
      SigningAlgorithms: ["RSASSA_PKCS1_V1_5_SHA_256"],
    });
    const response = await kmsService.getPublicKey();

    expect(response).toEqual(
      "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAuA1gxsWNOVSboz38+wAAeqKjq+yudtNpfg+xUuLKDLp+KcvYU84oSlxe1h4cCAwEAAQ=",
    );
  });
});
