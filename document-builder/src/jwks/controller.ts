import { NextFunction, Request, Response } from "express";
import { KmsService } from "../services/kmsService";
import { getClientSigningKeyId } from "../config/appConfig";
import { buildJwks } from "./jwksBuilder";

export async function jwksGetController(
  _req: Request,
  res: Response,
  next: NextFunction,
): Promise<void> {
  try {
    const keyId = getClientSigningKeyId();
    const kmsService = new KmsService(keyId);
    const jwks = await buildJwks(keyId, kmsService);
    res.json(jwks);
  } catch (error) {
    next(error);
  }
}
