import express from "express";
import { credentialViewerController } from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.VIEW_CREDENTIAL,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  credentialViewerController,
);

export { router as credentialViewerRouter };
