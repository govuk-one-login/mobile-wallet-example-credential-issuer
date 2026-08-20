import { getMockReq, getMockRes } from "@jest-mock/express";
import { createJwksController } from "../../src/jwks/controller";
import { KmsService } from "../../src/services/kmsService";

describe("jwksController", () => {
  const rsaPublicKeyBase64 =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArfFYx70x2REo13FfqFdqISe6cjz6morHuzyDLKawLDeHTwOy62BP4aZykWR04CKXD6EjB/P67UrfieeqzobNfrVnLP6WD7qkzc0K9Y8qjG/iHKoR7FqL1C/5Yt46RoAy3kOqC3YllicqsWOPqEWcw+gxwwkZ9q50mxSMyf+KimINA/f7mvGmxyzyVmxfzwtdKgbKPknm2VJZivGlCB//DUEF+Gzz5Axuo/NOrwddig0oQSn0Pzy3RfQ6ZBWdsiUBW3dTYy96018xe0uw8kR8m+zFwXkfFp9/MMc99bOpCfL/2UVU9135HvfojRSbIYL/s20aCHfc5o04pw9oY2+WDwIDAQAB";

  const mockKeyId = "test-key-id-123";

  it("should return a JWKS JSON response", async () => {
    const mockKmsService = {
      getPublicKey: jest.fn().mockResolvedValue(rsaPublicKeyBase64),
    } as unknown as KmsService;

    const controller = createJwksController(mockKeyId, mockKmsService);

    const req = getMockReq();
    const { res, next } = getMockRes();

    await controller(req, res, next);

    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        keys: expect.arrayContaining([
          expect.objectContaining({
            kty: "RSA",
            use: "sig",
            kid: mockKeyId,
            e: "AQAB",
          }),
        ]),
      }),
    );
  });

  it("should call next with error when KMS fails", async () => {
    const mockKmsService = {
      getPublicKey: jest.fn().mockRejectedValue(new Error("KMS unavailable")),
    } as unknown as KmsService;

    const controller = createJwksController(mockKeyId, mockKmsService);

    const req = getMockReq();
    const { res, next } = getMockRes();

    await controller(req, res, next);

    expect(next).toHaveBeenCalledWith(expect.any(Error));
    expect(res.json).not.toHaveBeenCalled();
  });
});
