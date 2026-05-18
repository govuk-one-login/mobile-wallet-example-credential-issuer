import axios from "axios";
import {
  getJwks,
  getMetadata,
  getDidDocument,
  getCredential,
  sendNotification,
  getDockerDnsName,
} from "./api";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("API utility functions", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("getDockerDnsName", () => {
    it("should replace localhost with host.docker.internal", () => {
      expect(getDockerDnsName("http://localhost:3000/foo")).toBe(
        "http://host.docker.internal:3000/foo",
      );
    });
    it("should return URL unchanged if not localhost", () => {
      expect(getDockerDnsName("http://example.com")).toBe("http://example.com");
    });
  });

  describe("getJwks", () => {
    it("should call axios.get with correct URL", async () => {
      mockedAxios.get.mockResolvedValue({ data: "jwks" });
      await getJwks("http://localhost:3000");
      expect(mockedAxios.get).toHaveBeenCalledWith(
        "http://host.docker.internal:3000/.well-known/jwks.json",
      );
    });

    it("throws API_ERROR on failure", async () => {
      mockedAxios.get.mockRejectedValue(new Error("fail"));
      await expect(getJwks("http://localhost:3000")).rejects.toThrow(
        /API_ERROR: Error trying to fetch/,
      );
    });
  });

  describe("getMetadata", () => {
    it("should call axios.get with correct URL", async () => {
      mockedAxios.get.mockResolvedValue({ data: "metadata" });
      await getMetadata("http://localhost:3000");
      expect(mockedAxios.get).toHaveBeenCalledWith(
        "http://host.docker.internal:3000/.well-known/openid-credential-issuer",
      );
    });
  });

  describe("getDidDocument", () => {
    it("should call axios.get with correct URL", async () => {
      mockedAxios.get.mockResolvedValue({ data: "did" });
      await getDidDocument("http://localhost:3000");
      expect(mockedAxios.get).toHaveBeenCalledWith(
        "http://host.docker.internal:3000/.well-known/did.json",
      );
    });
  });

  describe("getCredential", () => {
    it("should call axios.post with correct request body and authorisation header", async () => {
      mockedAxios.post.mockResolvedValue({ data: "credential" });
      await getCredential(
        "token123",
        "jwt456",
        "http://localhost:3000/credential",
      );
      expect(mockedAxios.post).toHaveBeenCalledWith(
        "http://host.docker.internal:3000/credential",
        {
          proof: {
            proof_type: "jwt",
            jwt: "jwt456",
          },
        },
        {
          headers: {
            Authorization: "Bearer token123",
            "Content-Type": "application/json",
          },
        },
      );
    });
  });

  describe("sendNotification", () => {
    it("should call axios.post with correct request body and authorisation header", async () => {
      mockedAxios.post.mockResolvedValue({ data: "notified" });
      await sendNotification(
        "token123",
        "notif-1",
        "EVENT_TYPE",
        "http://localhost:3000/notify",
      );
      expect(mockedAxios.post).toHaveBeenCalledWith(
        "http://host.docker.internal:3000/notify",
        {
          notification_id: "notif-1",
          event: "EVENT_TYPE",
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer token123",
          },
        },
      );
    });

    it("should call axios.post without authorisation header if accessToken is undefined", async () => {
      mockedAxios.post.mockResolvedValue({ data: "notified" });
      await sendNotification(
        undefined,
        "notif-1",
        "EVENT_TYPE",
        "http://localhost:3000/notify",
      );
      expect(mockedAxios.post).toHaveBeenCalledWith(
        "http://host.docker.internal:3000/notify",
        {
          notification_id: "notif-1",
          event: "EVENT_TYPE",
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
        },
      );
    });
  });
});
