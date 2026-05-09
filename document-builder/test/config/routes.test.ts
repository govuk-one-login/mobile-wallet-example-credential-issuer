import {
  ROUTES,
  dvsRoutes,
  gdsRoutes,
  commonRoutes,
} from "../../src/config/routes";

describe("routes", () => {
  describe("ROUTES", () => {
    it("should define all route paths", () => {
      expect(ROUTES.DVS_START).toBe("/dvs/start");
      expect(ROUTES.DVS_SELECT_JOURNEY).toBe("/dvs/select-journey");
      expect(ROUTES.DVS_BUILD_TEST_DRIVING_LICENCE).toBe(
        "/dvs/build-driving-licence",
      );
      expect(ROUTES.DVS_CREDENTIAL_OFFER_VIEWER).toBe(
        "/dvs/view-credential-offer/:itemId",
      );
      expect(ROUTES.START).toBe("/start");
      expect(ROUTES.SELECT_APP).toBe("/select-app");
      expect(ROUTES.SELECT_DOCUMENT).toBe("/select-document");
      expect(ROUTES.BUILD_NINO_DOCUMENT).toBe("/build-nino-document");
      expect(ROUTES.BUILD_DBS_DOCUMENT).toBe("/build-dbs-document");
      expect(ROUTES.BUILD_VETERAN_CARD_DOCUMENT).toBe(
        "/build-veteran-card-document",
      );
      expect(ROUTES.BUILD_DRIVING_LICENCE_DOCUMENT).toBe(
        "/build-driving-licence",
      );
      expect(ROUTES.BUILD_SIMPLE_DOCUMENT).toBe("/build-simple-document");
      expect(ROUTES.LOGGED_OUT).toBe("/logged-out");
      expect(ROUTES.PROOF_JWT).toBe("/proof-jwt");
      expect(ROUTES.REFRESH).toBe("/refresh/:credentialType");
      expect(ROUTES.REFRESH_NO_UPDATE).toBe(
        "/refresh/:credentialType/no-update",
      );
      expect(ROUTES.WELL_KNOWN_JWKS).toBe("/.well-known/jwks.json");
      expect(ROUTES.TOKEN).toBe("/token");
      expect(ROUTES.LOGOUT).toBe("/logout");
      expect(ROUTES.RETURN_FROM_AUTH).toBe("/return-from-auth");
      expect(ROUTES.VIEW_CREDENTIAL).toBe("/view-credential");
      expect(ROUTES.CREDENTIAL_OFFER_VIEWER).toBe(
        "/view-credential-offer/:itemId",
      );
      expect(ROUTES.DOCUMENT).toBe("/document/:itemId");
      expect(ROUTES.REVOKE).toBe("/revoke");
    });
  });

  describe("dvsRoutes", () => {
    it("should contain DVS routes", () => {
      expect(dvsRoutes).toEqual([
        ROUTES.DVS_START,
        ROUTES.DVS_SELECT_JOURNEY,
        ROUTES.DVS_BUILD_TEST_DRIVING_LICENCE,
        ROUTES.DVS_CREDENTIAL_OFFER_VIEWER,
      ]);
    });
  });

  describe("gdsRoutes", () => {
    it("should contain GDS routes", () => {
      expect(gdsRoutes).toEqual([
        ROUTES.START,
        ROUTES.SELECT_APP,
        ROUTES.SELECT_DOCUMENT,
        ROUTES.BUILD_NINO_DOCUMENT,
        ROUTES.BUILD_DBS_DOCUMENT,
        ROUTES.BUILD_VETERAN_CARD_DOCUMENT,
        ROUTES.BUILD_DRIVING_LICENCE_DOCUMENT,
        ROUTES.BUILD_SIMPLE_DOCUMENT,
        ROUTES.LOGGED_OUT,
        ROUTES.PROOF_JWT,
        ROUTES.REFRESH,
        ROUTES.REFRESH_NO_UPDATE,
        ROUTES.WELL_KNOWN_JWKS,
        ROUTES.TOKEN,
        ROUTES.LOGOUT,
        ROUTES.RETURN_FROM_AUTH,
        ROUTES.VIEW_CREDENTIAL,
        ROUTES.CREDENTIAL_OFFER_VIEWER,
      ]);
    });
  });

  describe("commonRoutes", () => {
    it("should contain common routes", () => {
      expect(commonRoutes).toEqual([ROUTES.REVOKE, ROUTES.DOCUMENT]);
    });
  });

  describe("route coverage", () => {
    it("should cover all routes in combination of commonRoutes, gdsRoutes and dvsRoutes", () => {
      const allRouteValues = Object.values(ROUTES);
      const combinedRoutes = [...commonRoutes, ...gdsRoutes, ...dvsRoutes];

      allRouteValues.forEach((route) => {
        expect(combinedRoutes).toContain(route);
      });
    });

    it("should have no repeated values across commonRoutes, gdsRoutes and dvsRoutes", () => {
      const combinedRoutes = [...commonRoutes, ...gdsRoutes, ...dvsRoutes];
      const uniqueRoutes = new Set(combinedRoutes);

      expect(combinedRoutes.length).toBe(uniqueRoutes.size);
    });
  });
});
