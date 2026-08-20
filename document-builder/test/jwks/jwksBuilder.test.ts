import { buildJwks } from "../../src/jwks/jwksBuilder";
import { KmsService } from "../../src/services/kmsService";

describe("buildJwks", () => {
  const rsaPublicKeyBase64 =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArfFYx70x2REo13FfqFdqISe6cjz6morHuzyDLKawLDeHTwOy62BP4aZykWR04CKXD6EjB/P67UrfieeqzobNfrVnLP6WD7qkzc0K9Y8qjG/iHKoR7FqL1C/5Yt46RoAy3kOqC3YllicqsWOPqEWcw+gxwwkZ9q50mxSMyf+KimINA/f7mvGmxyzyVmxfzwtdKgbKPknm2VJZivGlCB//DUEF+Gzz5Axuo/NOrwddig0oQSn0Pzy3RfQ6ZBWdsiUBW3dTYy96018xe0uw8kR8m+zFwXkfFp9/MMc99bOpCfL/2UVU9135HvfojRSbIYL/s20aCHfc5o04pw9oY2+WDwIDAQAB";

  const mockKeyId = "test-key-id-123";

  it("should return a JWKS with the correct structure", async () => {
    const mockKmsService = {
      getPublicKey: jest.fn().mockResolvedValue(rsaPublicKeyBase64),
    } as unknown as KmsService;

    const jwks = await buildJwks(mockKeyId, mockKmsService);

    expect(jwks).toHaveProperty("keys");
    expect(jwks.keys).toHaveLength(1);

    const key = jwks.keys[0];
    expect(key.kty).toBe("RSA");
    expect(key.use).toBe("sig");
    expect(key.kid).toBe(mockKeyId);
    expect(key.e).toBe("AQAB");
    expect(key.n).toBeDefined();
  });

  it("should propagate errors from KMS", async () => {
    const mockKmsService = {
      getPublicKey: jest.fn().mockRejectedValue(new Error("KMS unavailable")),
    } as unknown as KmsService;

    await expect(buildJwks(mockKeyId, mockKmsService)).rejects.toThrow(
      "KMS unavailable",
    );
  });
});
