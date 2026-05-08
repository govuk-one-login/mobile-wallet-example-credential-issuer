import express from "express";
import { startGetController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(ROUTES.START, guardRouteByEnvironment(), startGetController);

export { router as startRouter };
