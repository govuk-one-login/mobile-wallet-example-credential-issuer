import express from "express";
import {
  appSelectorGetController,
  appSelectorPostController,
} from "./controller";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { ROUTES } from "../config/routes";
import { requiresAuth } from "../middleware/requiresAuth";

const router = express.Router();

router.get(
  ROUTES.SELECT_APP,
  guardRouteByEnvironment(),
  requiresAuth,
  appSelectorGetController(),
);
router.post(
  ROUTES.SELECT_APP,
  guardRouteByEnvironment(),
  requiresAuth,
  appSelectorPostController(),
);

export { router as appSelectorRouter };
