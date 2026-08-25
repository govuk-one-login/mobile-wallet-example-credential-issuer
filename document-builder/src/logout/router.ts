import express from "express";
import { logoutGetController } from "./controller";
import { requiresAuth } from "../middleware/requiresAuth";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(
  ROUTES.LOGOUT,
  guardRouteByEnvironment(),
  requiresAuth,
  logoutGetController(),
);

export { router as logoutRouter };
