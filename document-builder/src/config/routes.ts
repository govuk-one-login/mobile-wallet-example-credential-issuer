export const ROUTES = {
  // DVS Routes
  DVS_START: "/dvs/start",
  DVS_SELECT_JOURNEY: "/dvs/select-journey",
  DVS_BUILD_TEST_DRIVING_LICENCE: "/dvs/build-driving-licence",
  DVS_CREDENTIAL_OFFER_VIEWER: "/dvs/view-credential-offer/:itemId",
  // GDS Routes
  START: "/start",
  SELECT_APP: "/select-app",
  SELECT_DOCUMENT: "/select-document",
  BUILD_NINO_DOCUMENT: "/build-nino-document",
  BUILD_DBS_DOCUMENT: "/build-dbs-document",
  BUILD_VETERAN_CARD_DOCUMENT: "/build-veteran-card-document",
  BUILD_DRIVING_LICENCE_DOCUMENT: "/build-driving-licence",
  BUILD_SIMPLE_DOCUMENT: "/build-simple-document",
  LOGGED_OUT: "/logged-out",
  PROOF_JWT: "/proof-jwt",
  REFRESH: "/refresh/:credentialType",
  REFRESH_NO_UPDATE: "/refresh/:credentialType/no-update",
  WELL_KNOWN_JWKS: "/.well-known/jwks.json",
  TOKEN: "/token",
  LOGOUT: "/logout",
  RETURN_FROM_AUTH: "/return-from-auth",
  VIEW_CREDENTIAL: "/view-credential",
  CREDENTIAL_OFFER_VIEWER: "/view-credential-offer/:itemId",
  // Common Routes
  DOCUMENT: "/document/:itemId",
  REVOKE: "/revoke",
};

export const dvsRoutes = [
  ROUTES.DVS_START,
  ROUTES.DVS_SELECT_JOURNEY,
  ROUTES.DVS_BUILD_TEST_DRIVING_LICENCE,
  ROUTES.DVS_CREDENTIAL_OFFER_VIEWER,
];
export const gdsRoutes = [
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
];
export const commonRoutes = [ROUTES.REVOKE, ROUTES.DOCUMENT];
