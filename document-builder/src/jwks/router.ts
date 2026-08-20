import express from "express";
import { KmsService } from "../services/kmsService";
import { createJwksController } from "./controller";

export function createJwksRouter(keyId: string): express.Router {
  const router = express.Router();
  const kmsService = new KmsService(keyId);
  const controller = createJwksController(keyId, kmsService);

  router.get("/.well-known/jwks.json", controller);

  return router;
}
