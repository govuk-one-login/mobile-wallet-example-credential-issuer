import {
  getClientId,
  getCredentialOfferDeepLink,
  getCriUrl,
  getPortNumber,
  getSelfURL,
  getWalletSubjectId,
} from "./config";

console.log = jest.fn();

describe("config", () => {
  it("should throw error when PORT is not set", async () => {
    process.env.PORT = "";
    expect(() => getPortNumber()).toThrow("PORT environment variable not set");
  });

  it("should throw error when CRI_URL is not set", async () => {
    process.env.CRI_URL = "";
    expect(() => {
      getCriUrl();
    }).toThrow("CRI_URL environment variable not set");
  });

  it("should return CRI_URL value when set", async () => {
    process.env.CRI_URL = "https://cri.test.gov.uk";
    expect(getCriUrl()).toEqual("https://cri.test.gov.uk");
  });

  it("should throw error when TEST_HARNESS_URL is not set", async () => {
    process.env.TEST_HARNESS_URL = "";
    expect(() => {
      getSelfURL();
    }).toThrow("TEST_HARNESS_URL environment variable not set");
  });

  it("should return TEST_HARNESS_URL value when set", async () => {
    process.env.TEST_HARNESS_URL = "https://test-harness.test.gov.uk";
    expect(getSelfURL()).toEqual("https://test-harness.test.gov.uk");
  });

  it("should throw error when CREDENTIAL_OFFER_DEEP_LINK is not set", async () => {
    expect(() => {
      getCredentialOfferDeepLink();
    }).toThrow("CREDENTIAL_OFFER_DEEP_LINK environment variable not set");
  });

  it("should throw error when WALLET_SUBJECT_ID is not set", async () => {
    expect(() => {
      getWalletSubjectId();
    }).toThrow("WALLET_SUBJECT_ID environment variable not set");
  });

  it("should throw error when CLIENT_ID is not set", async () => {
    expect(() => {
      getClientId();
    }).toThrow("CLIENT_ID environment variable not set");
  });
});
