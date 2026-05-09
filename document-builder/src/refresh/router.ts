import express from "express";
import {
  refreshGetController,
  refreshNoUpdateGetController,
  refreshPostController,
} from "./controller";
import { validateCredentialTypePath } from "../middleware/validateCredentialTypePath";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { ROUTES } from "../config/routes";

const router = express.Router();

router.get(
  ROUTES.REFRESH,
  guardRouteByEnvironment(),
  validateCredentialTypePath,
  refreshGetController,
);
router.post(
  ROUTES.REFRESH,
  guardRouteByEnvironment(),
  validateCredentialTypePath,
  refreshPostController,
);
router.get(
  ROUTES.REFRESH_NO_UPDATE,
  guardRouteByEnvironment(),
  validateCredentialTypePath,
  refreshNoUpdateGetController,
);

export { router as refreshRouter };
