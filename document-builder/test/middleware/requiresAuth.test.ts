import { NextFunction } from "express";
import { requiresAuth } from "../../src/middleware/requiresAuth";
import { getMockReq, getMockRes } from "@jest-mock/express";
import {
  COOKIE_TTL_IN_MILLISECONDS,
  getHardcodedWalletSubjectId,
} from "../../src/config/appConfig";

process.env.SELF = "http://localhost:3000";

const COOKIE_OPTIONS = { httpOnly: true, maxAge: COOKIE_TTL_IN_MILLISECONDS };

describe("requiresAuth", () => {
  describe("when auth is disabled", () => {
    beforeEach(() => {
      process.env.ENVIRONMENT = "local";
    });

    afterEach(() => {
      delete process.env.ENVIRONMENT;
    });

    it("should call next and set stub cookies when not already present", () => {
      /* eslint-disable  @typescript-eslint/no-explicit-any */
      const req = getMockReq({ cookies: {} }) as any;
      const { res } = getMockRes();
      const next: NextFunction = jest.fn();

      requiresAuth(req, res, next);

      expect(next).toHaveBeenCalledTimes(1);
      expect(res.redirect).not.toHaveBeenCalled();
      expect(res.cookie).toHaveBeenCalledWith(
        "id_token",
        "stub-id-token",
        COOKIE_OPTIONS,
      );
      expect(res.cookie).toHaveBeenCalledWith(
        "wallet_subject_id",
        getHardcodedWalletSubjectId(),
        COOKIE_OPTIONS,
      );
    });

    it("should not overwrite cookies that are already present", () => {
      const req = getMockReq({
        cookies: {
          id_token: "existing-token",
          wallet_subject_id: "existing-subject",
        },
      }) as any;
      const { res } = getMockRes();
      const next: NextFunction = jest.fn();

      requiresAuth(req, res, next);

      expect(next).toHaveBeenCalledTimes(1);
      expect(res.cookie).not.toHaveBeenCalledWith(
        "id_token",
        expect.anything(),
        expect.anything(),
      );
      expect(res.cookie).not.toHaveBeenCalledWith(
        "wallet_subject_id",
        expect.anything(),
        expect.anything(),
      );
    });
  });

  describe("when auth is enabled", () => {
    beforeEach(() => {
      process.env.ENVIRONMENT = "build";
    });

    afterEach(() => {
      delete process.env.ENVIRONMENT;
    });

    it("should redirect to authorisation URL and set cookies when user is not authenticated", () => {
      const authorizationUrl = "https://auth.test/authorize";
      const req = getMockReq({
        url: "/test-protected",
        cookies: {
          app: "govuk-staging",
          // missing id_token => not authenticated
        },
        oidc: {
          authorizationUrl: jest.fn().mockReturnValue(authorizationUrl),
          metadata: {
            scopes: "openid",
            redirect_uris: ["http://localhost/callback"],
            client_id: "test-client",
          },
        },
        /* eslint-disable  @typescript-eslint/no-explicit-any */
      }) as any;
      const { res } = getMockRes();
      const nextFunction: NextFunction = jest.fn();

      requiresAuth(req, res, nextFunction);

      expect(req.oidc.authorizationUrl).toHaveBeenCalledTimes(1);
      expect(res.redirect).toHaveBeenCalledWith(authorizationUrl);
      expect(res.cookie).toHaveBeenCalledWith(
        "nonce",
        expect.any(String),
        expect.objectContaining({
          httpOnly: true,
          maxAge: COOKIE_TTL_IN_MILLISECONDS,
        }),
      );
      expect(res.cookie).toHaveBeenCalledWith(
        "state",
        expect.any(String),
        expect.objectContaining({
          httpOnly: true,
          maxAge: COOKIE_TTL_IN_MILLISECONDS,
        }),
      );
      expect(res.cookie).toHaveBeenCalledWith(
        "current_url",
        "/test-protected",
        expect.objectContaining({
          httpOnly: true,
          maxAge: COOKIE_TTL_IN_MILLISECONDS,
        }),
      );
      expect(nextFunction).not.toHaveBeenCalled();
    });

    it("should call next when user is authenticated and app cookie is present", () => {
      const req = getMockReq({
        cookies: {
          app: "govuk-staging",
          id_token: "some-id-token",
        },
      });
      const { res } = getMockRes();
      const nextFunction: NextFunction = jest.fn();

      requiresAuth(req, res, nextFunction);

      expect(nextFunction).toHaveBeenCalledTimes(1);
      expect(res.redirect).not.toHaveBeenCalled();
    });
  });
});
