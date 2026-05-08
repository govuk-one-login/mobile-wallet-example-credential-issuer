import {
  safeDecodeJwt,
  decodeCredentialAsJwt,
} from "../../../src/credentialViewer/decoders/jwtDecoder";

describe("jwtDecoder", () => {
  describe("safeDecodeJwt", () => {
    it("should decode a valid JWT token", () => {
      // JWT with payload: {"sub":"1234567890","name":"John Doe","iat":1516239022}
      const validJwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

      const result = safeDecodeJwt(validJwt, "Test error message");

      expect(result).toBeDefined();
      expect(result?.sub).toBe("1234567890");
      expect(result?.name).toBe("John Doe");
      expect(result?.iat).toBe(1516239022);
    });

    it("should return undefined for invalid JWT and not throw", () => {
      const invalidJwt = "not-a-valid-jwt";

      const result = safeDecodeJwt(invalidJwt, "Test error message");

      expect(result).toBeUndefined();
    });

    it("should return undefined for empty string", () => {
      const result = safeDecodeJwt("", "Test error message");

      expect(result).toBeUndefined();
    });

    it("should return undefined for JWT with invalid base64", () => {
      const invalidBase64Jwt = "eyJ!!!.eyJ!!!.sig";

      const result = safeDecodeJwt(invalidBase64Jwt, "Test error message");

      expect(result).toBeUndefined();
    });

    it("should decode JWT with complex claims", () => {
      // JWT with payload containing arrays and nested objects
      // {"iss":"http://localhost:8080","aud":"http://localhost:8001","credential_identifiers":["id1","id2"],"exp":1736445301}
      const complexJwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImlkMSIsImlkMiJdLCJleHAiOjE3MzY0NDUzMDF9.placeholder";

      const result = safeDecodeJwt(complexJwt, "Test error message");

      expect(result).toBeDefined();
      expect(result?.iss).toBe("http://localhost:8080");
      expect(result?.aud).toBe("http://localhost:8001");
      expect(result?.credential_identifiers).toEqual(["id1", "id2"]);
      expect(result?.exp).toBe(1736445301);
    });
  });

  describe("decodeCredentialAsJwt", () => {
    it("should return credential data with decoded claims for valid JWT", () => {
      const validJwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

      const result = decodeCredentialAsJwt(validJwt);

      expect(result.credentialClaims).toBeDefined();
      expect(result.credentialClaimsTitle).toBe("VCDM credential");
      expect(result.x5chain).toBe("");
      expect(result.x5chainHex).toBe("");
      expect(result.credentialSignature).toBeUndefined();
      expect(result.credentialSignaturePayload).toBeUndefined();
    });

    it("should return credential data with undefined claims for invalid JWT", () => {
      const invalidJwt = "invalid-jwt";

      const result = decodeCredentialAsJwt(invalidJwt);

      expect(result.credentialClaims).toBeUndefined();
      expect(result.credentialClaimsTitle).toBe("VCDM credential");
      expect(result.x5chain).toBe("");
      expect(result.x5chainHex).toBe("");
    });

    it("should always return VCDM credential as title", () => {
      const validJwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.placeholder";

      const result = decodeCredentialAsJwt(validJwt);

      expect(result.credentialClaimsTitle).toBe("VCDM credential");
    });
  });
});
