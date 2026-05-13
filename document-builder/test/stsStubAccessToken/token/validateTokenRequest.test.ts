import {
  getPreAuthorizedCodePayload,
  validateGrantType,
} from "../../../src/stsStubAccessToken/token/validateTokenRequest";

describe("validateTokenRequest.ts", () => {
  describe("validateGrantType", () => {
    it("should return true if grant type is valid", async () => {
      const response = validateGrantType(
        "urn:ietf:params:oauth:grant-type:pre-authorized_code",
      );

      expect(response).toEqual(true);
    });

    it("should return false if grant type is invalid", async () => {
      const response = validateGrantType("something:else");

      expect(response).toEqual(false);
    });
  });

  describe("getPreAuthorizedCodePayload", () => {
    it("should return claims if pre-authorized code is valid", async () => {
      const response = getPreAuthorizedCodePayload(
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJpc3MiOiJ1cm46ZmRjOmdvdjp1azo8SE1SQz4iLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbIjFjZGZjMTY5LTJmYzQtNDQ4Yy05ZjgxLTdiNjkyOTYwY2UxMyJdfQ.sL1Rj4FC-U9HOvnjrjSDbieMdVaJt5JipBNy4xWmXzU",
      );

      expect(response).toEqual({
        aud: "urn:fdc:gov:uk:wallet",
        credential_identifiers: ["1cdfc169-2fc4-448c-9f81-7b692960ce13"],
        iss: "urn:fdc:gov:uk:<HMRC>",
      });
    });

    it("should throw an error if pre-authorized code is not a valid JWT", async () => {
      expect(() => getPreAuthorizedCodePayload("not.valid.jwt")).toThrow(
        new Error("Failed to parse the decoded payload as JSON"),
      );
    });

    it("should return false if pre-authorized code is missing 'aud' claim", async () => {
      const response = getPreAuthorizedCodePayload(
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ1cm46ZmRjOmdvdjp1azo8SE1SQz4iLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbIjFjZGZjMTY5LTJmYzQtNDQ4Yy05ZjgxLTdiNjkyOTYwY2UxMyJdfQ.Jp-ZyQ8mZLHVsv4gV7yx5gVjLPNQ8tVZcP2Q92aleW8",
      );

      expect(response).toEqual(false);
    });

    it("should return false if pre-authorized code is invalid missing 'iss' claim", async () => {
      const response = getPreAuthorizedCodePayload(
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbIjFjZGZjMTY5LTJmYzQtNDQ4Yy05ZjgxLTdiNjkyOTYwY2UxMyJdfQ.CzUBQdmyy1Ib3QfaWeZ2BQVA1MUqILhKk_H226XadFQ",
      );

      expect(response).toEqual(false);
    });

    it("should return false if pre-authorized code is missing 'credential_identifiers' claim", async () => {
      const response = getPreAuthorizedCodePayload(
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJpc3MiOiJ1cm46ZmRjOmdvdjp1azo8SE1SQz4ifQ.1YCC5Vh3TofwNd3TPXtggdo0gsWNqOV_zHqakSylb3k",
      );

      expect(response).toEqual(false);
    });
  });
});
