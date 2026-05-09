import express from "express";
import { dvsDrivingLicenceBuilderGetController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(
  ROUTES.DVS_BUILD_TEST_DRIVING_LICENCE,
  guardRouteByEnvironment(),
  dvsDrivingLicenceBuilderGetController,
);

export { router as dvsDrivingLicenceBuilderRouter };
