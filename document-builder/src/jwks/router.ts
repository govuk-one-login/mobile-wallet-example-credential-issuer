import express from "express";
import { jwksGetController } from "./controller";

const router = express.Router();

router.get("/.well-known/jwks.json", jwksGetController);

export { router as jwksRouter };
