import { getMockReq, getMockRes } from "@jest-mock/express";
import { logoutGetController } from "../../src/logout/controller";
import "../../src/types/Request";
import * as utils from "../../src/logout/utils/deleteCookies";

jest.mock("../../src/logout/utils/deleteCookies", () => ({
  deleteCookies: jest.fn(),
}));

const deleteCookies = utils.deleteCookies as jest.Mock;

describe("logoutGetController", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  const config = { selfUrl: "http://test-stub.com" };

  it("should call next with an error when an exception is thrown", () => {
    const req = getMockReq({
      cookies: {
        get id_token(): string {
          throw new Error("unexpected error");
        },
      },
      oidc: { endSessionUrl: jest.fn() },
    });
    const { res, next } = getMockRes();

    logoutGetController(config)(req, res, next);

    expect(next).toHaveBeenCalledWith(
      expect.objectContaining({
        message: "An error happened trying to logout",
      }),
    );
  });

  it("should redirect user to One Login to log out", () => {
    const req = getMockReq({
      oidc: { endSessionUrl: jest.fn() },
      cookies: {
        id_token: "id_token",
        access_token: "access_token",
        state: "state",
        app: "govuk-staging",
      },
    });
    const { res, next } = getMockRes({
      cookies: {
        id_token: "id_token",
        access_token: "access_token",
        state: "state",
        app: "govuk-staging",
      },
    });

    logoutGetController(config)(req, res, next);

    expect(res.redirect).toHaveBeenCalled();
    expect(req.oidc.endSessionUrl).toHaveBeenCalled();
    expect(deleteCookies).toHaveBeenCalledWith(req, res, [
      "id_token",
      "state",
      "nonce",
      "app",
      "wallet_subject_id",
      "current_url",
    ]);
  });
});
