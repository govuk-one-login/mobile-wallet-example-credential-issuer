import { getMockReq, getMockRes } from "@jest-mock/express";
import { returnFromAuthGetController } from "../../src/returnFromAuth/controller";
import { logger } from "../../src/middleware/logger";
import * as assertionJwt from "../../src/returnFromAuth/clientAssertion/buildClientAssertion";
import { COOKIE_TTL_IN_MILLISECONDS } from "../../src/config/appConfig";

process.env.CLIENT_SIGNING_KEY_ID = "14122ec4-cdd0-4154-8275-04363c15fbd9";

const WALLET_SUBJECT_ID =
  "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";

jest.mock(
  "../../src/returnFromAuth/clientAssertion/buildClientAssertion",
  () => ({
    buildClientAssertion: jest.fn(),
  }),
);

describe("returnFromAuthGetController", () => {
  const buildAssertionJwt = assertionJwt.buildClientAssertion as jest.Mock;
  const loggerErrorSpy = jest
    .spyOn(logger, "error")
    .mockImplementation(() => undefined);

  const createOidcMockReq = (overrides = {}) => {
    const userinfo = { wallet_subject_id: WALLET_SUBJECT_ID };
    return getMockReq({
      cookies: { nonce: "test_nonce", state: "test_state" },
      oidc: {
        callbackParams: jest.fn().mockReturnValue({ code: "auth_code_123" }),
        callback: jest.fn().mockResolvedValue({
          access_token: "access_token",
          id_token: "id_token",
        }),
        metadata: {
          client_id: "test_client_id",
          redirect_uris: ["http://localhost:3000/test"],
        },
        issuer: {
          metadata: {
            token_endpoint: "http://localhost:8000/token",
          },
        },
        userinfo: jest.fn().mockImplementation(() => userinfo),
      },
      ...overrides,
    });
  };

  beforeEach(() => {
    jest.clearAllMocks();
    loggerErrorSpy.mockReset();
    buildAssertionJwt.mockResolvedValue("clientAssertionJWT");
  });

  it("should call callbackParams with request when processing the callback", async () => {
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(req.oidc!.callbackParams).toHaveBeenCalledWith(req);
  });

  it("should render error page when error is present in callback params", async () => {
    const req = getMockReq({
      oidc: {
        callbackParams: jest.fn().mockReturnValue({
          error: "some_error",
          error_description: "Some error description",
        }),
      },
    });
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(loggerErrorSpy).toHaveBeenCalledWith(
      { error: "some_error", error_description: "Some error description" },
      "OAuth authorization failed",
    );
    expect(res.render).toHaveBeenCalledWith("500.njk");
    expect(res.redirect).not.toHaveBeenCalled();
    expect(buildAssertionJwt).not.toHaveBeenCalled();
  });

  it("should call buildAssertionJwt with the correct params", async () => {
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(buildAssertionJwt).toHaveBeenCalledWith(
      "test_client_id",
      "http://localhost:8000/token",
      "14122ec4-cdd0-4154-8275-04363c15fbd9",
    );
  });

  it("should render error page on client assertion building failure", async () => {
    const error = new Error("JWT signing failed");
    buildAssertionJwt.mockRejectedValue(error);
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(loggerErrorSpy).toHaveBeenCalledWith(
      "OAuth callback failed: JWT signing failed",
    );
    expect(res.render).toHaveBeenCalledWith("500.njk");
    expect(res.redirect).not.toHaveBeenCalled();
  });

  it("should properly log string errors", async () => {
    const error = "String error";
    buildAssertionJwt.mockRejectedValue(error);
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(loggerErrorSpy).toHaveBeenCalledWith(
      "OAuth callback failed: String error",
    );
    expect(res.render).toHaveBeenCalledWith("500.njk");
  });

  it("should call OIDC callback with the correct params", async () => {
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(req.oidc!.callback).toHaveBeenCalledWith(
      "http://localhost:3000/test",
      { code: "auth_code_123" },
      { nonce: "test_nonce", state: "test_state" },
      {
        exchangeBody: {
          client_assertion_type:
            "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
          client_assertion: "clientAssertionJWT",
        },
      },
    );
  });

  it("should render error page on OIDC callback failure", async () => {
    const errorMessage = "Token exchange failed";
    const req = createOidcMockReq();
    req.oidc!.callback = jest.fn().mockRejectedValue({ message: errorMessage });
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(loggerErrorSpy).toHaveBeenCalledWith(
      "OAuth callback failed: Token exchange failed",
    );
    expect(res.render).toHaveBeenCalledWith("500.njk");
    expect(res.redirect).not.toHaveBeenCalled();
  });

  it("should call userinfo with the correct params", async () => {
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(req.oidc!.userinfo).toHaveBeenCalledWith("access_token", {
      method: "GET",
      via: "header",
    });
  });

  it("should render error page on userinfo failure", async () => {
    const errorMessage = "Failed to get user info";
    const req = createOidcMockReq();
    req.oidc!.userinfo = jest.fn().mockRejectedValue({ message: errorMessage });
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(loggerErrorSpy).toHaveBeenCalledWith(
      "OAuth callback failed: Failed to get user info",
    );
    expect(res.render).toHaveBeenCalledWith("500.njk");
    expect(res.redirect).not.toHaveBeenCalled();
  });

  it("should set id_token and wallet_subject_id cookies after successful callback", async () => {
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(res.cookie).toHaveBeenNthCalledWith(1, "id_token", "id_token", {
      httpOnly: true,
      maxAge: COOKIE_TTL_IN_MILLISECONDS,
    });
    expect(res.cookie).toHaveBeenNthCalledWith(
      2,
      "wallet_subject_id",
      WALLET_SUBJECT_ID,
      { httpOnly: true, maxAge: COOKIE_TTL_IN_MILLISECONDS },
    );
    expect(loggerErrorSpy).not.toHaveBeenCalled();
  });

  it("should redirect to current_url cookie when present", async () => {
    const req = createOidcMockReq({
      cookies: {
        nonce: "test_nonce",
        state: "test_state",
        current_url: "/protected",
      },
    });
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(res.redirect).toHaveBeenCalledWith("/protected");
    expect(loggerErrorSpy).not.toHaveBeenCalled();
  });

  it("should redirect to default URL /select-document when current_url not present", async () => {
    const req = createOidcMockReq();
    const { res } = getMockRes();

    await returnFromAuthGetController(req, res);

    expect(res.redirect).toHaveBeenCalledWith("/select-app");
    expect(loggerErrorSpy).not.toHaveBeenCalled();
  });
});
