import express from "express";
import { stsStubAccessTokenController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.post(
  ROUTES.TOKEN,
  guardRouteByEnvironment(),
  stsStubAccessTokenController,
);

export { router as stsStubAccessTokenRouter };
