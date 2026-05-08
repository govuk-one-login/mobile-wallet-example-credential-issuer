import express from "express";
import {
  drivingLicenceBuilderGetController,
  drivingLicenceBuilderPostController,
} from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.BUILD_DRIVING_LICENCE_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  drivingLicenceBuilderGetController(),
);
router.post(
  ROUTES.BUILD_DRIVING_LICENCE_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  drivingLicenceBuilderPostController(),
);

export { router as drivingLicenceBuilderRouter };
