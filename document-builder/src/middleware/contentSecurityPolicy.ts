import crypto from "node:crypto";
import type { Request, Response, NextFunction } from "express";
import helmet from "helmet";

export function generateCspNonce(
  _req: Request,
  res: Response,
  next: NextFunction,
): void {
  res.locals.cspNonce = crypto.randomBytes(16).toString("base64");
  next();
}

export function getFormActionSources(): string[] {
  return ["'self'"];
}

export function buildContentSecurityPolicy() {
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
        formAction: getFormActionSources(),
      },
    },
  });
}
