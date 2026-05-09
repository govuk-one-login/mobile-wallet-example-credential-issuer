import { NextFunction } from "express";
import { getMockReq, getMockRes } from "@jest-mock/express";
import { guardRouteByEnvironment } from "../../src/middleware/guardRouteByEnvironment";
import { ROUTES } from "../../src/config/routes";
import { ENVIRONMENTS } from "../../src/config/environments";

const SELF_URL = "http://localhost";

describe("guardRouteByEnvironment", () => {
  describe("Common Routes", () => {
    describe(ROUTES.REVOKE, () => {
      it.each([
        [ENVIRONMENTS.LOCAL],
        [ENVIRONMENTS.DEV],
        [ENVIRONMENTS.BUILD],
        [ENVIRONMENTS.STAGE],
        [ENVIRONMENTS.INT],
      ])("should allow in %s environment", (environment) => {
        const req = getMockReq({ path: ROUTES.REVOKE });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, environment)(req, res, next);

        expect(next).toHaveBeenCalled();
        expect(res.redirect).not.toHaveBeenCalled();
      });
    });

    describe(ROUTES.DOCUMENT, () => {
      it.each([
        [ENVIRONMENTS.LOCAL],
        [ENVIRONMENTS.DEV],
        [ENVIRONMENTS.BUILD],
        [ENVIRONMENTS.STAGE],
        [ENVIRONMENTS.INT],
      ])("should allow in %s environment", (environment) => {
        const req = getMockReq({ path: "/document/1234" });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, environment)(req, res, next);

        expect(next).toHaveBeenCalled();
        expect(res.redirect).not.toHaveBeenCalled();
      });

      it("should allow with special characters in itemId", () => {
        const req = getMockReq({ path: "/document/abc-123_xyz" });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, ENVIRONMENTS.DEV)(req, res, next);

        expect(next).toHaveBeenCalled();
      });
    });
  });

  describe("DVS Routes", () => {
    describe.each([
      [ROUTES.DVS_START, ROUTES.DVS_START],
      [ROUTES.DVS_SELECT_JOURNEY, ROUTES.DVS_SELECT_JOURNEY],
      [
        ROUTES.DVS_CREDENTIAL_OFFER_VIEWER,
        "/dvs/view-credential-offer/abc-123",
      ],
    ])("%s", (routeName, testPath) => {
      it.each([
        [ENVIRONMENTS.LOCAL],
        [ENVIRONMENTS.DEV],
        [ENVIRONMENTS.BUILD],
        [ENVIRONMENTS.INT],
      ])("should allow in %s environment", (environment) => {
        const req = getMockReq({ path: testPath });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, environment)(req, res, next);

        expect(next).toHaveBeenCalled();
        expect(res.redirect).not.toHaveBeenCalled();
      });

      it("should redirect in STAGE environment", () => {
        const req = getMockReq({ path: testPath });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, ENVIRONMENTS.STAGE)(req, res, next);

        expect(next).not.toHaveBeenCalled();
        expect(res.redirect).toHaveBeenCalledWith(`${SELF_URL}/start`);
      });
    });

    describe("DVS_CREDENTIAL_OFFER_VIEWER edge cases", () => {
      it("should allow with special characters in itemId", () => {
        const req = getMockReq({
          path: "/dvs/view-credential-offer/abc-123_xyz",
        });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, ENVIRONMENTS.INT)(req, res, next);

        expect(next).toHaveBeenCalled();
      });
    });
  });

  describe("GDS Routes", () => {
    describe.each([
      [ROUTES.START, ROUTES.START],
      [ROUTES.SELECT_APP, ROUTES.SELECT_APP],
      [ROUTES.SELECT_DOCUMENT, ROUTES.SELECT_DOCUMENT],
      [ROUTES.BUILD_NINO_DOCUMENT, ROUTES.BUILD_NINO_DOCUMENT],
      [ROUTES.BUILD_DBS_DOCUMENT, ROUTES.BUILD_DBS_DOCUMENT],
      [ROUTES.BUILD_VETERAN_CARD_DOCUMENT, ROUTES.BUILD_VETERAN_CARD_DOCUMENT],
      [
        ROUTES.BUILD_DRIVING_LICENCE_DOCUMENT,
        ROUTES.BUILD_DRIVING_LICENCE_DOCUMENT,
      ],
      [ROUTES.BUILD_SIMPLE_DOCUMENT, ROUTES.BUILD_SIMPLE_DOCUMENT],
      [ROUTES.LOGGED_OUT, ROUTES.LOGGED_OUT],
      [ROUTES.PROOF_JWT, ROUTES.PROOF_JWT],
      [ROUTES.REFRESH, "/refresh/nino"],
      [ROUTES.REFRESH_NO_UPDATE, "/refresh/nino/no-update"],
      [ROUTES.WELL_KNOWN_JWKS, ROUTES.WELL_KNOWN_JWKS],
      [ROUTES.TOKEN, ROUTES.TOKEN],
      [ROUTES.LOGOUT, ROUTES.LOGOUT],
      [ROUTES.RETURN_FROM_AUTH, ROUTES.RETURN_FROM_AUTH],
      [ROUTES.VIEW_CREDENTIAL, ROUTES.VIEW_CREDENTIAL],
      [ROUTES.VIEW_CREDENTIAL, "/view-credential/"],
      [ROUTES.CREDENTIAL_OFFER_VIEWER, "/view-credential-offer/abc-123"],
    ])("%s", (routeName, testPath) => {
      it.each([
        [ENVIRONMENTS.LOCAL],
        [ENVIRONMENTS.DEV],
        [ENVIRONMENTS.BUILD],
        [ENVIRONMENTS.STAGE],
      ])("should allow in %s environment", (environment) => {
        const req = getMockReq({ path: testPath });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, environment)(req, res, next);

        expect(next).toHaveBeenCalled();
        expect(res.redirect).not.toHaveBeenCalled();
      });

      it("should redirect in INT environment", () => {
        const req = getMockReq({ path: testPath });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, ENVIRONMENTS.INT)(req, res, next);

        expect(next).not.toHaveBeenCalled();
        expect(res.redirect).toHaveBeenCalledWith(`${SELF_URL}/dvs/start`);
      });
    });

    describe("CREDENTIAL_OFFER_VIEWER edge cases", () => {
      it("should allow with special characters in itemId", () => {
        const req = getMockReq({
          path: "/view-credential-offer/abc-123_xyz",
        });
        const { res } = getMockRes();
        const next: NextFunction = jest.fn();

        guardRouteByEnvironment(SELF_URL, ENVIRONMENTS.STAGE)(req, res, next);

        expect(next).toHaveBeenCalled();
      });
    });
  });

  describe("redirects for mismatched routes and environments", () => {
    it("should redirect to /dvs/start when accessing GDS route in DVS environment", () => {
      const req = getMockReq({ path: ROUTES.SELECT_APP });
      const { res } = getMockRes();
      const next: NextFunction = jest.fn();

      guardRouteByEnvironment("http://localhost", ENVIRONMENTS.INT)(
        req,
        res,
        next,
      );

      expect(next).not.toHaveBeenCalled();
      expect(res.redirect).toHaveBeenCalledWith("http://localhost/dvs/start");
    });

    it("should redirect to /start when accessing DVS route in GDS environment", () => {
      const req = getMockReq({ path: ROUTES.DVS_START });
      const { res } = getMockRes();
      const next: NextFunction = jest.fn();

      guardRouteByEnvironment("http://localhost", ENVIRONMENTS.STAGE)(
        req,
        res,
        next,
      );

      expect(next).not.toHaveBeenCalled();
      expect(res.redirect).toHaveBeenCalledWith("http://localhost/start");
    });

    it("should redirect to / for unknown environment", () => {
      const req = getMockReq({ path: ROUTES.SELECT_APP });
      const { res } = getMockRes();
      const next: NextFunction = jest.fn();

      guardRouteByEnvironment("http://localhost", "unknown")(req, res, next);

      expect(next).not.toHaveBeenCalled();
      expect(res.redirect).toHaveBeenCalledWith("/");
    });
  });
});
