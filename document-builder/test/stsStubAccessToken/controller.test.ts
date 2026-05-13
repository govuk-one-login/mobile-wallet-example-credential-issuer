process.env.STS_SIGNING_KEY_ID = "mock_signing_key_id";
import { stsStubAccessTokenController } from "../../src/stsStubAccessToken/controller";
import { getMockReq, getMockRes } from "@jest-mock/express";
import * as accessToken from "../../src/stsStubAccessToken/token/createAccessToken";
import * as validateTokenRequest from "../../src/stsStubAccessToken/token/validateTokenRequest";
import { ACCESS_TOKEN_TTL_IN_SECS } from "../../src/config/appConfig";

jest.mock("../../src/stsStubAccessToken/token/validateTokenRequest", () => ({
  validateGrantType: jest.fn(),
  getPreAuthorizedCodePayload: jest.fn(),
}));
jest.mock("../../src/stsStubAccessToken/token/createAccessToken", () => ({
  createAccessToken: jest.fn(),
}));

describe("controller.ts", () => {
  it("should return 200 and the bearer access token in the response body", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code":
          "eyJhbciOiJIUzIIkpXVCJ9.eyJzdWIiOiMjMwjoxNTE2MjM5MQ.SflKRJT4fwpeJ36POk6yJV_adQssw5c",
      },
    });

    const validateGrantType =
      validateTokenRequest.validateGrantType as jest.Mock;
    validateGrantType.mockReturnValueOnce(true);

    const getPreAuthorizedCodePayload =
      validateTokenRequest.getPreAuthorizedCodePayload as jest.Mock;
    getPreAuthorizedCodePayload.mockReturnValueOnce({ mock: "payload" });

    const createAccessToken = accessToken.createAccessToken as jest.Mock;
    createAccessToken.mockReturnValueOnce(
      "eyJ0eXAiOiJKV1Qh.eyJzdWIiOiM.9nQevZ--Asqx5ltCWvw_AvVNDA",
    );

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      access_token: "eyJ0eXAiOiJKV1Qh.eyJzdWIiOiM.9nQevZ--Asqx5ltCWvw_AvVNDA",
      expires_in: ACCESS_TOKEN_TTL_IN_SECS,
      token_type: "bearer",
    });
  });

  it("should return 401 if 'pre-authorized_code' is equal to 'ERROR:401'", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code": "ERROR:401",
      },
    });

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(undefined);
  });

  it("should return 400 and 'invalid_client' if 'pre-authorized_code' is equal to 'ERROR:CLIENT'", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code": "ERROR:CLIENT",
      },
    });

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: "invalid_client" });
  });

  it("should return 400 and 'invalid_grant' if 'pre-authorized_code' is equal to 'ERROR:GRANT'", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code": "ERROR:GRANT",
      },
    });

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: "invalid_grant" });
  });

  it("should return 500 and 'server_error' if 'pre-authorized_code' is equal to 'ERROR:500'", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code": "ERROR:500",
      },
    });

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: "server_error" });
  });

  it("should return 400 and 'invalid_grant' if 'grant_type' is invalid", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "invalid_grant_type",
        "pre-authorized_code":
          "eyJhbciOiJIUzIIkpXVCJ9.eyJzdWIiOiMjMwjoxNTE2MjM5MQ.SflKRJT4fwpeJ36POk6yJV_adQssw5c",
      },
    });

    const validateGrantType =
      validateTokenRequest.validateGrantType as jest.Mock;
    validateGrantType.mockReturnValueOnce(false);

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      error: "invalid_grant",
    });
  });

  it("should return 400 and 'invalid_grant' if 'pre-authorized_code' is invalid", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code": "invalid_pre-authorized_code",
      },
    });

    const validateGrantType =
      validateTokenRequest.validateGrantType as jest.Mock;
    validateGrantType.mockReturnValueOnce(true);

    const getPreAuthorizedCodePayload =
      validateTokenRequest.getPreAuthorizedCodePayload as jest.Mock;
    getPreAuthorizedCodePayload.mockReturnValueOnce(false);

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      error: "invalid_grant",
    });
  });

  it("should return 500 and 'server_error' if an unexpected error happens", async () => {
    const { res } = getMockRes();
    const req = getMockReq({
      body: {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code":
          "eyJhbciOiJIUzIIkpXVCJ9.eyJzdWIiOiMjMwjoxNTE2MjM5MQ.SflKRJT4fwpeJ36POk6yJV_adQssw5c",
      },
    });

    const validateGrantType =
      validateTokenRequest.validateGrantType as jest.Mock;
    validateGrantType.mockReturnValueOnce(true);

    const getPreAuthorizedCodePayload =
      validateTokenRequest.getPreAuthorizedCodePayload as jest.Mock;
    getPreAuthorizedCodePayload.mockReturnValueOnce({ mock: "payload" });

    const createAccessToken = accessToken.createAccessToken as jest.Mock;
    createAccessToken.mockRejectedValueOnce(new Error("SOME_ERROR"));

    await stsStubAccessTokenController(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      error: "server_error",
    });
  });
});
