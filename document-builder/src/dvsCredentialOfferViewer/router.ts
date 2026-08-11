import express from "express";
import { dvsCredentialOfferViewerController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { validateItemId } from "../middleware/validateItemId";

const router = express.Router();

router.get(
  ROUTES.DVS_CREDENTIAL_OFFER_VIEWER,
  guardRouteByEnvironment(),
  validateItemId,
  dvsCredentialOfferViewerController(),
);

export { router as dvsCredentialOfferViewerRouter };
