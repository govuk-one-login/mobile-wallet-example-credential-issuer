process.env.SELF = "https://doc-builder.test";
process.env.CREDENTIAL_ISSUER_URL = "https://example-cri.test";
process.env.ONE_LOGIN_AUTH_SERVER_URL = "https://sts-mock.test";

import axios, { AxiosResponse } from "axios";
import {
  getAccessToken,
  getProofJwt,
  getCredential,
} from "../../../src/credentialViewer/services/credentialService";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("credentialService", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("getAccessToken", () => {
    it("should exchange pre-authorized code for access token", async () => {
      const preAuthorizedCode = "test-pre-auth-code";
      const expectedAccessToken = "test-access-token";

      mockedAxios.post.mockResolvedValueOnce({
        data: { access_token: expectedAccessToken },
      } as AxiosResponse);

      const result = await getAccessToken(preAuthorizedCode);

      expect(result).toBe(expectedAccessToken);
      expect(mockedAxios.post).toHaveBeenCalledWith(
        "https://sts-mock.test/token",
        {
          grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
          "pre-authorized_code": preAuthorizedCode,
        },
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        },
      );
    });

    it("should propagate errors from axios", async () => {
      const preAuthorizedCode = "test-pre-auth-code";
      const error = new Error("Network error");

      mockedAxios.post.mockRejectedValueOnce(error);

      await expect(getAccessToken(preAuthorizedCode)).rejects.toThrow(
        "Network error",
      );
    });
  });

  describe("getProofJwt", () => {
    it("should fetch proof JWT with nonce and audience", async () => {
      const nonce = "test-nonce";
      const audience = "https://example.com";
      const expectedProofJwt = "test-proof-jwt";

      mockedAxios.get.mockResolvedValueOnce({
        data: { proofJwt: expectedProofJwt },
      } as AxiosResponse);

      const result = await getProofJwt(nonce, audience);

      expect(result).toBe(expectedProofJwt);
      expect(mockedAxios.get).toHaveBeenCalledWith(
        `https://doc-builder.test/proof-jwt?nonce=${nonce}&audience=${audience}`,
      );
    });

    it("should handle special characters in audience URL", async () => {
      const nonce = "test-nonce";
      const audience = "https://example.com/path?param=value";
      const expectedProofJwt = "test-proof-jwt";

      mockedAxios.get.mockResolvedValueOnce({
        data: { proofJwt: expectedProofJwt },
      } as AxiosResponse);

      const result = await getProofJwt(nonce, audience);

      expect(result).toBe(expectedProofJwt);
    });

    it("should propagate errors from axios", async () => {
      const error = new Error("Network error");

      mockedAxios.get.mockRejectedValueOnce(error);

      await expect(getProofJwt("nonce", "audience")).rejects.toThrow(
        "Network error",
      );
    });
  });

  describe("getCredential", () => {
    it("should fetch credential with access token and proof JWT", async () => {
      const accessToken = "test-access-token";
      const proofJwt = "test-proof-jwt";
      const expectedCredential = "test-credential";

      mockedAxios.post.mockResolvedValueOnce({
        data: {
          credentials: [{ credential: expectedCredential }],
        },
      } as AxiosResponse);

      const result = await getCredential(accessToken, proofJwt);

      expect(result).toBe(expectedCredential);
      expect(mockedAxios.post).toHaveBeenCalledWith(
        "https://example-cri.test/credential",
        {
          proof: {
            proof_type: "jwt",
            jwt: proofJwt,
          },
        },
        {
          headers: {
            Authorization: `BEARER ${accessToken}`,
          },
        },
      );
    });

    it("should extract first credential from response array", async () => {
      const accessToken = "test-access-token";
      const proofJwt = "test-proof-jwt";
      const firstCredential = "first-credential";
      const secondCredential = "second-credential";

      mockedAxios.post.mockResolvedValueOnce({
        data: {
          credentials: [
            { credential: firstCredential },
            { credential: secondCredential },
          ],
        },
      } as AxiosResponse);

      const result = await getCredential(accessToken, proofJwt);

      expect(result).toBe(firstCredential);
    });

    it("should propagate errors from axios", async () => {
      const error = new Error("Credential issuer error");

      mockedAxios.post.mockRejectedValueOnce(error);

      await expect(getCredential("token", "proof")).rejects.toThrow(
        "Credential issuer error",
      );
    });

    it("should use BEARER token in Authorization header", async () => {
      const accessToken = "my-access-token";

      mockedAxios.post.mockResolvedValueOnce({
        data: { credentials: [{ credential: "cred" }] },
      } as AxiosResponse);

      await getCredential(accessToken, "proof");

      expect(mockedAxios.post).toHaveBeenCalledWith(
        expect.any(String),
        expect.any(Object),
        expect.objectContaining({
          headers: {
            Authorization: "BEARER my-access-token",
          },
        }),
      );
    });
  });
});
