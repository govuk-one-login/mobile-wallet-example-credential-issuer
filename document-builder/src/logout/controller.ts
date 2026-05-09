import { Request, Response } from "express";
import { logger } from "../middleware/logger";
import { getSelfUrl } from "../config/appConfig";
import { deleteCookies } from "./utils/deleteCookies";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";

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
  return function (req: Request, res: Response): void {
    try {
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
      logger.error(error, "An error happened trying to logout");
      res.render("500.njk");
    }
  };
}
