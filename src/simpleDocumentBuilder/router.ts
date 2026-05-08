import { requiresAuth } from "../middleware/requiresAuth";
import { requiresAppSelected } from "../middleware/requiresAppSelected";
import express from "express";
import {
  simpleDocumentBuilderGetController,
  simpleDocumentBuilderPostController,
} from "./controller";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { ROUTES } from "../config/routes";

const router = express.Router();

router.get(
  ROUTES.BUILD_SIMPLE_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  simpleDocumentBuilderGetController(),
);
router.post(
  ROUTES.BUILD_SIMPLE_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  simpleDocumentBuilderPostController(),
);

export { router as simpleDocumentBuilderRouter };
