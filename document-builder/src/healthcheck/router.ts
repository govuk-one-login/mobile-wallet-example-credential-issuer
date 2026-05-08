import express from "express";
import { healthcheckGetController } from "./controller";

const router = express.Router();

router.get("/healthcheck", healthcheckGetController);

export { router as healthcheckRouter };
