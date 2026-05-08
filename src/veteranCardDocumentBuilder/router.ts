import express from "express";
import {
  veteranCardDocumentBuilderGetController,
  veteranCardDocumentBuilderPostController,
} from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { ROUTES } from "../config/routes";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.BUILD_VETERAN_CARD_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  veteranCardDocumentBuilderGetController(),
);
router.post(
  ROUTES.BUILD_VETERAN_CARD_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  veteranCardDocumentBuilderPostController,
);

export { router as veteranCardDocumentBuilderRouter };
