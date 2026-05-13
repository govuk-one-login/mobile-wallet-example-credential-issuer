import { isValidCredentialOffer } from "./isValidCredentialOffer";

describe("isValidCredentialOffer", () => {
  it("should return 'true' when credential offer is valid", async () => {
    const credentialOffer =
      "https://mobile.build.account.gov.uk/wallet-test/add?credential_offer=%7B%22credential_configuration_ids%22%3A%5B%22SocialSecurityCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJlNDJjNmM2Zi1kMzhjLTQ0NjgtYjFiZC1jMDc2ZGUyMTAzYTIiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOTIwMDBmMDktMTEwMS00OGZlLWE0YjgtNDc2NGQyNjdjMTA0Il0sImV4cCI6MTcyMDA5OTg4NywiaWF0IjoxNzIwMDk5NTg3fQ.wbg668HQjpaKivpHZ2SBWNJHTbBa6df4mhKz0TITymiTxMsZOpXJDo_WxK-Urgwpf91J9iv-Oq34lslGNXgTug%22%7D%7D%2C%22credential_issuer%22%3A%22https%3A%2F%2Fexample-credential-issuer.mobile.dev.account.gov.uk%22%7D";
    expect(isValidCredentialOffer(credentialOffer)).toEqual(true);
  });

  it("should throw 'INVALID_DEEP_LINK' error if deep link is not a valid URL", async () => {
    const credentialOffer =
      "mobile.build.account.gov.uk/wallet-test/add?credential_offer=%7B%22credential_configuration_ids%22%3A%5B%22SocialSecurityCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJlNDJjNmM2Zi1kMzhjLTQ0NjgtYjFiZC1jMDc2ZGUyMTAzYTIiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOTIwMDBmMDktMTEwMS00OGZlLWE0YjgtNDc2NGQyNjdjMTA0Il0sImV4cCI6MTcyMDA5OTg4NywiaWF0IjoxNzIwMDk5NTg3fQ.wbg668HQjpaKivpHZ2SBWNJHTbBa6df4mhKz0TITymiTxMsZOpXJDo_WxK-Urgwpf91J9iv-Oq34lslGNXgTug%22%7D%7D%2C%22credential_issuer%22%3A%22https%3A%2F%2Fexample-credential-issuer.mobile.dev.account.gov.uk%22%7D";
    expect(() => isValidCredentialOffer(credentialOffer)).toThrow(
      "INVALID_DEEP_LINK",
    );
  });

  it("should throw 'MISSING_CREDENTIAL_OFFER' error if deep link does not contain a credential offer", async () => {
    const credentialOffer = "https://mobile.build.account.gov.uk/wallet-test/";
    expect(() => isValidCredentialOffer(credentialOffer)).toThrow(
      "INVALID_DEEP_LINK: Missing 'credential_offer'",
    );
  });

  it("should throw 'INVALID_JSON' error if credential offer is not a valid JSON", async () => {
    const credentialOffer =
      "https://mobile.build.account.gov.uk/wallet-test/add?credential_offer=%7B%22credential_configuration_ids%22%3A%5B%22SocialSecurityCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJlNDJjNmM2Zi1kMzhjLTQ0NjgtYjFiZC1jMDc2ZGUyMTAzYTIiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOTIwMDBmMDktMTEwMS00OGZlLWE0YjgtNDc2NGQyNjdjMTA0Il0sImV4cCI6MTcyMDA5OTg4NywiaWF0IjoxNzIwMDk5NTg3fQ.wbg668HQjpaKivpHZ2SBWNJHTbBa6df4mhKz0TITymiTxMsZOpXJDo_WxK-Urgwpf91J9iv-Oq34lslGNXgTug%22%7D%7D%2C%22credential_issuer%22%3A%22https%3A%2F%2Fexample-credential-issuer.mobile.dev.account.gov.uk%22%";
    expect(() => isValidCredentialOffer(credentialOffer)).toThrow(
      "INVALID_CREDENTIAL_OFFER: Not a valid JSON. {}",
    );
  });

  it("should throw 'INVALID_CREDENTIAL_OFFER' if credential offer is missing required parameter 'grants'", async () => {
    const credentialOffer =
      "https://mobile.build.account.gov.uk/wallet-test/add?credential_offer=%7B%22credential_configuration_ids%22%3A%5B%22SocialSecurityCredential%22%5D%2C%22missingGrants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJlNDJjNmM2Zi1kMzhjLTQ0NjgtYjFiZC1jMDc2ZGUyMTAzYTIiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiOTIwMDBmMDktMTEwMS00OGZlLWE0YjgtNDc2NGQyNjdjMTA0Il0sImV4cCI6MTcyMDA5OTg4NywiaWF0IjoxNzIwMDk5NTg3fQ.wbg668HQjpaKivpHZ2SBWNJHTbBa6df4mhKz0TITymiTxMsZOpXJDo_WxK-Urgwpf91J9iv-Oq34lslGNXgTug%22%7D%7D%2C%22credential_issuer%22%3A%22https%3A%2F%2Fexample-credential-issuer.mobile.dev.account.gov.uk%22%7D";
    expect(() => isValidCredentialOffer(credentialOffer)).toThrow(
      "INVALID_CREDENTIAL_OFFER",
    );
  });
});
