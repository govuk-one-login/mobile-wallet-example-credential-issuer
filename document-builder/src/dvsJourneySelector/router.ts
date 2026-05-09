import express from "express";
import {
  dvsJourneySelectorGetController,
  dvsJourneySelectorPostController,
} from "./controller";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";
import { ROUTES } from "../config/routes";

const router = express.Router();

router.get(
  ROUTES.DVS_SELECT_JOURNEY,
  guardRouteByEnvironment(),
  dvsJourneySelectorGetController,
);
router.post(
  ROUTES.DVS_SELECT_JOURNEY,
  guardRouteByEnvironment(),
  dvsJourneySelectorPostController,
);

export { router as dvsJourneySelectorRouter };
