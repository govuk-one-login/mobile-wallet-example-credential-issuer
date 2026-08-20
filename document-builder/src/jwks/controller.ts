import { NextFunction, Request, Response } from "express";
import { KmsService } from "../services/kmsService";
import { buildJwks } from "./jwksBuilder";

export function createJwksController(keyId: string, kmsService: KmsService) {
  return async function jwksGetController(
    _req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> {
    try {
      const jwks = await buildJwks(keyId, kmsService);
      res.json(jwks);
    } catch (error) {
      next(error);
    }
  };
}
