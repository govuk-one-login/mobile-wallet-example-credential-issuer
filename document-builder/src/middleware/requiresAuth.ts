import { NextFunction, Request, Response } from "express";
import {
  COOKIE_TTL_IN_MILLISECONDS,
  getHardcodedWalletSubjectId,
} from "../config/appConfig";
import { generators } from "openid-client";
import { logger } from "./logger";
import { isAuthDisabled } from "../config/environments";

const VECTORS_OF_TRUST = `["Cl"]`;

export function requiresAuth(
  req: Request,
  res: Response,
  next: NextFunction,
): void {
  if (isAuthDisabled()) {
    const cookieOptions = {
      httpOnly: true,
      maxAge: COOKIE_TTL_IN_MILLISECONDS,
    };
    if (!req.cookies["id_token"]) {
      res.cookie("id_token", "stub-id-token", cookieOptions);
    }
    if (!req.cookies["wallet_subject_id"]) {
      res.cookie(
        "wallet_subject_id",
        getHardcodedWalletSubjectId(),
        cookieOptions,
      );
    }
    return next();
  }

  const isAuthenticated = req.cookies["id_token"];

  logger.info(`isAuthenticated: ${isAuthenticated}`);

  if (isAuthenticated === undefined) {
    redirectToLogIn(req, res);
  } else {
    next();
  }
}

export function getAuthorizationUrl(req: Request, res: Response) {
  const nonce = generators.nonce();
  const state = generators.state();

  res.cookie("nonce", nonce, {
    httpOnly: true,
    maxAge: COOKIE_TTL_IN_MILLISECONDS,
  });
  res.cookie("state", state, {
    httpOnly: true,
    maxAge: COOKIE_TTL_IN_MILLISECONDS,
  });
  res.cookie("current_url", req.url, {
    httpOnly: true,
    maxAge: COOKIE_TTL_IN_MILLISECONDS,
  });

  return req.oidc.authorizationUrl({
    client_id: req.oidc.metadata.client_id,
    response_type: "code",
    prompt: "none",
    scope: req.oidc.metadata.scopes as string,
    state: state,
    nonce: nonce,
    redirect_uri: req.oidc.metadata.redirect_uris![0],
    cookie_consent: req.query.cookie_consent,
    vtr: VECTORS_OF_TRUST,
  });
}

function redirectToLogIn(req: Request, res: Response): void {
  const authorizationUrl = getAuthorizationUrl(req, res);
  return res.redirect(authorizationUrl);
}
