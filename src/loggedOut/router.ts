import express from "express";
import { loggedOutGetController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(
  ROUTES.LOGGED_OUT,
  guardRouteByEnvironment(),
  loggedOutGetController,
);

export { router as loggedOutRouter };
