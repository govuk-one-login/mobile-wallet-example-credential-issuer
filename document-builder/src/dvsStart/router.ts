import express from "express";
import { dvsStartGetController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(ROUTES.DVS_START, guardRouteByEnvironment(), dvsStartGetController);

export { router as dvsStartRouter };
