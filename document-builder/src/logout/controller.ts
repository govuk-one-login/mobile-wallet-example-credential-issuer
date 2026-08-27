import { NextFunction, Request, Response } from "express";
import { getSelfUrl } from "../config/appConfig";
import { deleteCookies } from "./utils/deleteCookies";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { isAuthDisabled } from "../config/environments";

const COOKIES_TO_DELETE = [
  "id_token",
  "state",
  "nonce",
  "app",
  "wallet_subject_id",
  "current_url",
];

export interface LogoutConfig {
  selfUrl?: string;
}

export function logoutGetController({
  selfUrl = getSelfUrl(),
}: LogoutConfig = {}): ExpressRouteFunction {
  return function (req: Request, res: Response, next: NextFunction): void {
    try {
      if (isAuthDisabled()) {
        return res.redirect(selfUrl + "/start");
      }

      const idToken = req.cookies.id_token;
      const state = req.cookies.state;
      deleteCookies(req, res, COOKIES_TO_DELETE);

      const postLogoutRedirectUri = selfUrl + "/logged-out";
      res.redirect(
        req.oidc.endSessionUrl({
          id_token_hint: idToken,
          post_logout_redirect_uri: postLogoutRedirectUri,
          state: state,
        }),
      );
    } catch (error) {
      next(new Error("An error happened trying to logout", { cause: error }));
    }
  };
}
