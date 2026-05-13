import express from "express";
import {
  ninoDocumentBuilderGetController,
  ninoDocumentBuilderPostController,
} from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.BUILD_NINO_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  ninoDocumentBuilderGetController(),
);
router.post(
  ROUTES.BUILD_NINO_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  ninoDocumentBuilderPostController,
);

export { router as ninoDocumentBuilderRouter };
