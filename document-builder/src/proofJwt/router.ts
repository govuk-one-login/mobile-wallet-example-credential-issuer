import express from "express";
import { proofJwtController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(ROUTES.PROOF_JWT, guardRouteByEnvironment(), proofJwtController);

export { router as proofJwtRouter };
