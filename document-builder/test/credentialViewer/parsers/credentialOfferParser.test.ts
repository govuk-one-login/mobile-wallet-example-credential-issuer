import { extractPreAuthCode } from "../../../src/credentialViewer/parsers/credentialOfferParser";

describe("credentialOfferParser", () => {
  describe("extractPreAuthCode", () => {
    it("should extract pre-authorized code from a valid credential offer URI", () => {
      const preAuthCode =
        "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";
      const credentialOfferUri = `https://mobile.dev.account.gov.uk/wallet-test/add?credential_offer={"grants":{"urn:ietf:params:oauth:grant-type:pre-authorized_code":{"pre-authorized_code":"${preAuthCode}"}},"credential_issuer":"http://localhost:8080"}`;

      const result = extractPreAuthCode(credentialOfferUri);

      expect(result).toBe(preAuthCode);
    });

    it("should handle URI with additional grant parameters", () => {
      const preAuthCode = "test-pre-auth-code";
      const credentialOfferUri = `https://example.com/wallet/add?credential_offer={"grants":{"urn:ietf:params:oauth:grant-type:pre-authorized_code":{"pre-authorized_code":"${preAuthCode}","other_param":"value"}},"credential_issuer":"http://localhost:8080"}`;

      const result = extractPreAuthCode(credentialOfferUri);

      expect(result).toBe(preAuthCode);
    });

    it("should handle URI with credential_configuration_ids", () => {
      const preAuthCode = "another-pre-auth-code";
      const credentialOfferUri = `https://example.com/wallet/add?credential_offer={"grants":{"urn:ietf:params:oauth:grant-type:pre-authorized_code":{"pre-authorized_code":"${preAuthCode}"}},"credential_issuer":"http://localhost:8080","credential_configuration_ids":["org.iso.18013.5.1.mDL"]}`;

      const result = extractPreAuthCode(credentialOfferUri);

      expect(result).toBe(preAuthCode);
    });

    it("should throw error for invalid JSON in credential offer", () => {
      const credentialOfferUri =
        "https://example.com/wallet/add?credential_offer=invalid-json";

      expect(() => extractPreAuthCode(credentialOfferUri)).toThrow();
    });

    it("should throw error for missing grants in credential offer", () => {
      const credentialOfferUri =
        'https://example.com/wallet/add?credential_offer={"credential_issuer":"http://localhost:8080"}';

      expect(() => extractPreAuthCode(credentialOfferUri)).toThrow();
    });

    it("should throw error for missing pre-authorized_code grant type", () => {
      const credentialOfferUri =
        'https://example.com/wallet/add?credential_offer={"grants":{"other_grant_type":{}},"credential_issuer":"http://localhost:8080"}';

      expect(() => extractPreAuthCode(credentialOfferUri)).toThrow();
    });
  });
});
