import {
  registerCborTagDecoders,
  decodeCredentialAsCbor,
} from "../../../src/credentialViewer/decoders/mdocDecoder";

describe("mdocDecoder", () => {
  describe("registerCborTagDecoders", () => {
    it("should be safe to call multiple times", () => {
      // Should not throw when called multiple times
      expect(() => {
        registerCborTagDecoders();
        registerCborTagDecoders();
        registerCborTagDecoders();
      }).not.toThrow();
    });
  });

  describe("decodeCredentialAsCbor", () => {
    it("should return mdoc Credential title even on decode failure", () => {
      const invalidCbor = "not-valid-base64url-cbor";

      const result = decodeCredentialAsCbor(invalidCbor);

      expect(result.credentialClaimsTitle).toBe("mdoc Credential");
    });

    it("should return undefined claims for invalid CBOR", () => {
      const invalidCbor = "invalid-cbor-data";

      const result = decodeCredentialAsCbor(invalidCbor);

      expect(result.credentialClaims).toBeUndefined();
      expect(result.x5chain).toBe("");
      expect(result.x5chainHex).toBe("");
    });

    it("should return empty x5chain for invalid credential", () => {
      const invalidCbor = "abc123";

      const result = decodeCredentialAsCbor(invalidCbor);

      expect(result.x5chain).toBe("");
      expect(result.x5chainHex).toBe("");
    });

    it("should handle empty string gracefully", () => {
      const result = decodeCredentialAsCbor("");

      expect(result.credentialClaimsTitle).toBe("mdoc Credential");
      expect(result.credentialClaims).toBeUndefined();
    });

    // Integration test with real mDoc credential data
    // it("should decode a valid mDoc credential", () => {
    //   // This is a simplified valid CBOR structure for testing
    //   // In practice, you'd use a real mDoc credential from test fixtures
    //   const validMdocCredential =
    //     "v2puYW1lU3BhY2Vzv3g8dWsuZ292LmFjY291bnQubW9iaWxlLmV4YW1wbGUtY3JlZGVudGlhbC1pc3N1ZXIuc2ltcGxlbWRvYy4xn9gYWF-kaGRpZ2VzdElEGg_MGOZmcmFuZG9tUPiW3OHVTlUmP78rGl4QftxxZWxlbWVudElkZW50aWZpZXJsdHlwZV9vZl9maXNobGVsZW1lbnRWYWx1ZWtDb2Fyc2UgZmlzaNgYWF6kaGRpZ2VzdElEGg_MGUZmcmFuZG9tUKwBdkGnnZit49eKib2SS3pxZWxlbWVudElkZW50aWZpZXJ2bnVtYmVyX29mX2Zpc2hpbmdfcm9kc2xlbGVtZW50VmFsdWUC_19faXNzdWVyQXV0aIRDoQEmogRYMWludmFsaWQta2V5LWRhdGEtZm9yLXRlc3RpbmctcHVycG9zZXMtb25seS1ub3QtcmVhbBghWCBpbnZhbGlkLWtleS1kYXRhLWZvci10ZXN0aW5n";
    //
    //   // This will attempt to decode - may fail on signature validation
    //   // but should at least parse the CBOR structure
    //   const result = decodeCredentialAsCbor(validMdocCredential);
    //
    //   // Even if full decode fails, title should be set
    //   expect(result.credentialClaimsTitle).toBe("mdoc Credential");
    // });
  });
});
