import { Request, Response } from "express";
import { logger } from "../middleware/logger";
import { CallbackParamsType, TokenSet, UserinfoResponse } from "openid-client";
import { buildClientAssertion } from "./clientAssertion/buildClientAssertion";
import {
  getClientSigningKeyId,
  COOKIE_TTL_IN_MILLISECONDS,
} from "../config/appConfig";
import { Jwt } from "../types/Jwt";

/**
 * Handles the OAuth callback from the authorization server.
 *
 * This controller processes the authorization code received from the authorization server,
 * exchanges it for access and ID tokens, and sets cookies for the user session.
 *
 * @param req - Express request object containing OAuth callback parameters
 * @param res - Express response object for sending the response
 * @returns Promise<void>
 */
export async function returnFromAuthGetController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    const callbackParams: CallbackParamsType = req.oidc.callbackParams(req);
    if (callbackParams?.error) {
      logger.error(
        {
          error: callbackParams.error,
          error_description: callbackParams.error_description,
        },
        "OAuth authorization failed",
      );
      res.render("500.njk");
      return;
    }

    // Build JWT assertion for client authentication
    const clientAssertion: Jwt = await buildClientAssertion(
      req.oidc.metadata.client_id,
      req.oidc.issuer.metadata.token_endpoint!,
      getClientSigningKeyId(),
    );
    // Exchange the access code in the url parameters for an access token
    const tokenSet: TokenSet = await req.oidc.callback(
      req.oidc.metadata.redirect_uris![0],
      req.oidc.callbackParams(req), // Get all parameters to pass to the token exchange endpoint
      { nonce: req.cookies.nonce, state: req.cookies.state },
      {
        exchangeBody: {
          client_assertion_type:
            "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
          client_assertion: clientAssertion,
        },
      },
    );

    const accessToken = tokenSet.access_token!;
    const userInfo: UserinfoResponse = await req.oidc.userinfo(accessToken, {
      method: "GET",
      via: "header",
    });
    const cookieOptions = {
      httpOnly: true,
      maxAge: COOKIE_TTL_IN_MILLISECONDS,
    };

    res.cookie("id_token", tokenSet.id_token, cookieOptions);
    res.cookie("wallet_subject_id", userInfo.wallet_subject_id, cookieOptions);

    const redirectUri = req.cookies.current_url || "/select-app";
    res.redirect(redirectUri);
  } catch (error) {
    const message =
      /* eslint-disable @typescript-eslint/no-explicit-any */
      typeof error === "string" ? error : ((error as any)?.message ?? "");
    logger.error(`OAuth callback failed: ${message}`);
    res.render("500.njk");
    return;
  }
}
