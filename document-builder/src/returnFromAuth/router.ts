import express from "express";
import { returnFromAuthGetController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(
  ROUTES.RETURN_FROM_AUTH,
  guardRouteByEnvironment(),
  returnFromAuthGetController,
);

export { router as returnFromAuthRouter };
