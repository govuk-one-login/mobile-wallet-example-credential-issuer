import express from "express";
import { documentController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(ROUTES.DOCUMENT, guardRouteByEnvironment(), documentController);

export { router as documentRouter };
