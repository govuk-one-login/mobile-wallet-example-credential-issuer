import {
  isJwtFormat,
  processCredential,
} from "../../../src/credentialViewer/decoders/credentialDecoder";

describe("credentialDecoder", () => {
  describe("isJwtFormat", () => {
    it("should return true for JWT format (starts with eyJ)", () => {
      const jwtCredential =
        "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";

      expect(isJwtFormat(jwtCredential)).toBe(true);
    });

    it("should return true for any string starting with eyJ", () => {
      expect(isJwtFormat("eyJhbGciOiJIUzI1NiJ9")).toBe(true);
      expect(isJwtFormat("eyJ")).toBe(true);
      expect(isJwtFormat("eyJhbnl0aGluZw")).toBe(true);
    });

    it("should return false for CBOR/mDoc format (base64url encoded)", () => {
      // mDoc credentials typically start with different characters
      const mdocCredential = "v2puYW1lU3BhY2Vzv3g8dWsu...";

      expect(isJwtFormat(mdocCredential)).toBe(false);
    });

    it("should return false for strings not starting with eyJ", () => {
      expect(isJwtFormat("abc123")).toBe(false);
      expect(isJwtFormat("")).toBe(false);
      expect(isJwtFormat("eYJ")).toBe(false); // case sensitive
      expect(isJwtFormat("EYJ")).toBe(false);
      expect(isJwtFormat(" eyJ")).toBe(false); // leading space
    });

    it("should return false for base64url encoded CBOR", () => {
      // Typical mDoc credential prefix
      expect(isJwtFormat("v2puYW1l")).toBe(false);
      expect(isJwtFormat("o2d2ZXJzaW9u")).toBe(false);
    });
  });

  describe("processCredential", () => {
    it("should decode JWT credential when format is JWT", () => {
      const jwtCredential =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.signature";

      const result = processCredential(jwtCredential);

      expect(result.credentialClaimsTitle).toBe("VCDM credential");
      expect(result.credentialClaims).toBeDefined();
    });

    it("should attempt CBOR decoding when format is not JWT", () => {
      // This will fail to decode as valid CBOR but should return mdoc structure
      const nonJwtCredential = "abc123notvalidcbor";

      const result = processCredential(nonJwtCredential);

      expect(result.credentialClaimsTitle).toBe("mdoc Credential");
      // Claims will be undefined due to decode failure
      expect(result.credentialClaims).toBeUndefined();
    });

    it("should return x5chain fields empty for JWT credentials", () => {
      const jwtCredential =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.sig";

      const result = processCredential(jwtCredential);

      expect(result.x5chain).toBe("");
      expect(result.x5chainHex).toBe("");
    });
  });
});
