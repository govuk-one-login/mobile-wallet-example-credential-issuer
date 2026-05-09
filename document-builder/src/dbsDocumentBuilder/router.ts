import express from "express";
import {
  dbsDocumentBuilderGetController,
  dbsDocumentBuilderPostController,
} from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.BUILD_DBS_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  dbsDocumentBuilderGetController(),
);
router.post(
  ROUTES.BUILD_DBS_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  dbsDocumentBuilderPostController,
);

export { router as dbsDocumentBuilderRouter };
