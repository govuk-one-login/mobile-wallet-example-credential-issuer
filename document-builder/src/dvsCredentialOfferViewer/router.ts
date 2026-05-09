import express from "express";
import { dvsCredentialOfferViewerController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(
  ROUTES.DVS_CREDENTIAL_OFFER_VIEWER,
  guardRouteByEnvironment(),
  dvsCredentialOfferViewerController(),
);

export { router as dvsCredentialOfferViewerRouter };
