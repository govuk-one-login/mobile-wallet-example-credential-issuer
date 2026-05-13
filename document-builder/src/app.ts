import express from "express";
import { appSelectorRouter } from "./appSelector/router";
import { dbsDocumentBuilderRouter } from "./dbsDocumentBuilder/router";
import { credentialOfferViewerRouter } from "./credentialOfferViewer/router";
import { documentRouter } from "./document/router";
import { stsStubAccessTokenRouter } from "./stsStubAccessToken/router";
import nunjucks from "nunjucks";
import path from "node:path";
import { stsStubJwksRouter } from "./stsStubJwks/router";
import { documentSelectorRouter } from "./documentSelector/router";
import { ninoDocumentBuilderRouter } from "./ninoDocumentBuilder/router";
import { loggerMiddleware } from "./middleware/logger";
import { getOIDCConfig } from "./config/oidc";
import { auth } from "./middleware/auth";
import { isAuthDisabled } from "./config/environments";
import cookieParser from "cookie-parser";
import { returnFromAuthRouter } from "./returnFromAuth/router";
import { logoutRouter } from "./logout/router";
import { loggedOutRouter } from "./loggedOut/router";
import { noCacheMiddleware } from "./middleware/noCache";
import { proofJwtRouter } from "./proofJwt/router";
import { credentialViewerRouter } from "./credentialViewer/router";
import { veteranCardDocumentBuilderRouter } from "./veteranCardDocumentBuilder/router";
import { revokeRouter } from "./revoke/router";
import { refreshRouter } from "./refresh/router";
import { simpleDocumentBuilderRouter } from "./simpleDocumentBuilder/router";
import { drivingLicenceBuilderRouter } from "./drivingLicenceBuilder/router";
import { dvsStartRouter } from "./dvsStart/router";
import { dvsJourneySelectorRouter } from "./dvsJourneySelector/router";
import { dvsDrivingLicenceBuilderRouter } from "./dvsDrivingLicenceBuilder/router";
import { dvsCredentialOfferViewerRouter } from "./dvsCredentialOfferViewer/router";
import { startRouter } from "./start/router";
import { pageNotFound } from "./middleware/pageNotFound";
import { healthcheckRouter } from "./healthcheck/router";

const APP_VIEWS = [
  path.resolve("dist/appSelector/views"),
  path.resolve("dist/credentialOfferViewer/views"),
  path.resolve("dist/credentialViewer/views"),
  path.resolve("dist/documentSelector/views"),
  path.resolve("dist/dbsDocumentBuilder/views"),
  path.resolve("dist/loggedOut/views"),
  path.resolve("dist/drivingLicenceBuilder/views"),
  path.resolve("dist/simpleDocumentBuilder/views"),
  path.resolve("dist/ninoDocumentBuilder/views"),
  path.resolve("dist/refresh/views"),
  path.resolve("dist/revoke/views"),
  path.resolve("dist/veteranCardDocumentBuilder/views"),
  path.resolve("dist/dvsStart/views"),
  path.resolve("dist/dvsJourneySelector/views"),
  path.resolve("dist/dvsCredentialOfferViewer/views"),
  path.resolve("dist/start/views"),
  path.resolve("dist/views"),
  path.resolve("node_modules/govuk-frontend/dist"),
];

export async function createApp(): Promise<express.Application> {
  const app: express.Application = express();

  app.use(cookieParser());
  app.use(express.urlencoded({ extended: true }));

  app.set(
    "view engine",
    nunjucks
      .configure(APP_VIEWS, {
        express: app,
        noCache: true,
      })
      .addGlobal("govukRebrand", true),
  );

  app.use("/public", express.static(path.resolve("dist/public")));
  app.use("/assets", express.static(path.resolve("dist/assets")));

  app.use(loggerMiddleware);
  app.use((req, res, next) => {
    req.log = req.log.child({
      trace: res.locals.trace,
    });
    next();
  });
  app.use(noCacheMiddleware);

  app.use(healthcheckRouter);
  app.use(documentRouter);
  app.use(loggedOutRouter);
  app.use(proofJwtRouter);
  app.use(refreshRouter);
  app.use(revokeRouter);
  app.use(stsStubAccessTokenRouter);
  app.use(stsStubJwksRouter);
  app.use(dvsStartRouter);
  app.use(dvsJourneySelectorRouter);
  app.use(dvsDrivingLicenceBuilderRouter);
  app.use(dvsCredentialOfferViewerRouter);
  app.use(startRouter);

  const oidcConfig = getOIDCConfig();
  if (!isAuthDisabled() && oidcConfig.discoveryEndpoint) {
    app.use(auth(oidcConfig));
  }
  app.use(appSelectorRouter);
  app.use(credentialOfferViewerRouter);
  app.use(credentialViewerRouter);
  app.use(dbsDocumentBuilderRouter);
  app.use(documentSelectorRouter);
  app.use(logoutRouter);
  app.use(drivingLicenceBuilderRouter);
  app.use(simpleDocumentBuilderRouter);
  app.use(ninoDocumentBuilderRouter);
  app.use(returnFromAuthRouter);
  app.use(veteranCardDocumentBuilderRouter);

  app.use(pageNotFound);

  return app;
}
