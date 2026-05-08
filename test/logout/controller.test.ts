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
    const { res } = getMockRes({
      cookies: {
        id_token: "id_token",
        access_token: "access_token",
        state: "state",
        app: "govuk-staging",
      },
    });

    logoutGetController(config)(req, res);

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
