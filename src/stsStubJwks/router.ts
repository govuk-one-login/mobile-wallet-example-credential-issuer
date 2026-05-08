import express from "express";
import { stsStubJwksController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(
  ROUTES.WELL_KNOWN_JWKS,
  guardRouteByEnvironment(),
  stsStubJwksController,
);

export { router as stsStubJwksRouter };
