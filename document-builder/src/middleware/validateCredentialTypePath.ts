import { NextFunction, Request, Response } from "express";
import { logger } from "./logger";
import { CredentialType } from "../types/CredentialType";

export function validateCredentialTypePath(
  req: Request,
  _res: Response,
  next: NextFunction,
): void {
  const { credentialType } = req.params;
  if (!isValidCredentialType(credentialType)) {
    logger.error(
      { credentialType },
      "Invalid credential type path parameter provided",
    );
    return next(new Error("Invalid credential type path parameter provided"));
  }
  next();
}

function isValidCredentialType(type: unknown): type is CredentialType {
  return (
    typeof type === "string" &&
    Object.values(CredentialType).includes(type as CredentialType)
  );
}
