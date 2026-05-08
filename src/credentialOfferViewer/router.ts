import express from "express";
import { credentialOfferViewerController } from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.CREDENTIAL_OFFER_VIEWER,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  credentialOfferViewerController(),
);

export { router as credentialOfferViewerRouter };
