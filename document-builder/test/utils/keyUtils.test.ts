import {
  createJwkFromRawPublicKey,
  uint8ArrayToBase64,
} from "../../src/utils/keyUtils";

describe("keyUtils", () => {
  describe("uint8ArrayToBase64", () => {
    it("should convert a Uint8Array to a base64 string", () => {
      const input = new Uint8Array([72, 101, 108, 108, 111]);
      expect(uint8ArrayToBase64(input)).toBe("SGVsbG8=");
    });
  });

  describe("createJwkFromRawPublicKey", () => {
    it("should convert a DER-encoded EC public key to a JWK", () => {
      const ecPublicKeyBase64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECO6A8rvNKD/sVNQwswdrIwR5ThN0gEc1rHtRzs5BXVvQ21bG1y7/b97RcxzbcQH+P2ti2DhwGiM/HwN5Agtg/Q==";
      const rawPublicKey = Buffer.from(ecPublicKeyBase64, "base64");

      const jwk = createJwkFromRawPublicKey(rawPublicKey);

      expect(jwk.kty).toBe("EC");
      expect(jwk.x).toBeDefined();
      expect(jwk.y).toBeDefined();
    });

    it("should convert a DER-encoded RSA public key to a JWK", () => {
      const rsaPublicKeyBase64 =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArfFYx70x2REo13FfqFdqISe6cjz6morHuzyDLKawLDeHTwOy62BP4aZykWR04CKXD6EjB/P67UrfieeqzobNfrVnLP6WD7qkzc0K9Y8qjG/iHKoR7FqL1C/5Yt46RoAy3kOqC3YllicqsWOPqEWcw+gxwwkZ9q50mxSMyf+KimINA/f7mvGmxyzyVmxfzwtdKgbKPknm2VJZivGlCB//DUEF+Gzz5Axuo/NOrwddig0oQSn0Pzy3RfQ6ZBWdsiUBW3dTYy96018xe0uw8kR8m+zFwXkfFp9/MMc99bOpCfL/2UVU9135HvfojRSbIYL/s20aCHfc5o04pw9oY2+WDwIDAQAB";
      const rawPublicKey = Buffer.from(rsaPublicKeyBase64, "base64");

      const jwk = createJwkFromRawPublicKey(rawPublicKey);

      expect(jwk.kty).toBe("RSA");
      expect(jwk.n).toBeDefined();
      expect(jwk.e).toBe("AQAB");
    });
  });
});
