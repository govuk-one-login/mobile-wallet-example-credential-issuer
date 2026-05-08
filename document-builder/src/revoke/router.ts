import express from "express";
import { revokeGetController, revokePostController } from "./controller";
import { ROUTES } from "../config/routes";
import { guardRouteByEnvironment } from "../middleware/guardRouteByEnvironment";

const router = express.Router();

router.get(ROUTES.REVOKE, guardRouteByEnvironment(), revokeGetController());
router.post(ROUTES.REVOKE, guardRouteByEnvironment(), revokePostController());

export { router as revokeRouter };
