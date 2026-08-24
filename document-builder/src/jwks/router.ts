import express from "express";
import { jwksGetController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(ROUTES.JWKS, guardRouteByEnvironment(), jwksGetController);

export { router as jwksRouter };
