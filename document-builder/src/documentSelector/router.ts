import express from "express";
import {
  documentSelectorGetController,
  documentSelectorPostController,
} from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { ROUTES } from "../config/routes";
import { requiresAppSelected } from "../middleware/requiresAppSelected";

const router = express.Router();

router.get(
  ROUTES.SELECT_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  documentSelectorGetController(),
);
router.post(
  ROUTES.SELECT_DOCUMENT,
  guardRouteByEnvironment(),
  requiresAuth,
  requiresAppSelected,
  documentSelectorPostController(),
);

export { router as documentSelectorRouter };
