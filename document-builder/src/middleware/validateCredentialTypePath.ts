import { NextFunction, Request, Response } from "express";
import { logger } from "./logger";
import { CredentialType } from "../types/CredentialType";

export function validateCredentialTypePath(
  req: Request,
  res: Response,
  next: NextFunction,
): void {
  const { credentialType } = req.params;
  if (!isValidCredentialType(credentialType)) {
    logger.error(
      `Invalid credential type path parameter provided: ${credentialType}`,
    );
    return res.render("500.njk");
  }
  next();
}

function isValidCredentialType(type: unknown): type is CredentialType {
  return (
    typeof type === "string" &&
    Object.values(CredentialType).includes(type as CredentialType)
  );
}
