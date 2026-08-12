import crypto from "node:crypto";
import type { Request, Response, NextFunction } from "express";
import helmet from "helmet";
import { logger } from "./logger";

export function generateCspNonce(
  _req: Request,
  res: Response,
  next: NextFunction,
): void {
  res.locals.cspNonce = crypto.randomBytes(16).toString("base64");
  next();
}

export function getFormActionSources(oidcEndpoint?: string): string[] {
  const formActionSources: string[] = ["'self'"];
  if (oidcEndpoint) {
    try {
      const url = new URL(oidcEndpoint);
      formActionSources.push(url.origin);
    } catch (error) {
      logger.warn(
        { oidcEndpoint, error },
        "OIDC endpoint is not a valid URL, form-action CSP will only allow 'self'",
      );
    }
  }
  return formActionSources;
}

export function buildContentSecurityPolicy(oidcEndpoint?: string) {
  return helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: [
          "'self'",
          (_req, res) => `'nonce-${(res as Response).locals.cspNonce}'`,
        ],
        styleSrc: [
          "'self'",
          (_req, res) => `'nonce-${(res as Response).locals.cspNonce}'`,
        ],
        imgSrc: ["'self'", "data:"],
        fontSrc: ["'self'"],
        connectSrc: ["'self'"],
        formAction: getFormActionSources(oidcEndpoint),
      },
    },
  });
}
