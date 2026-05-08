import { Request, Response } from "express";
import { isAuthenticated } from "../utils/isAuthenticated";

export function refreshGetController(req: Request, res: Response): void {
  const { credentialType } = req.params;

  res.render("refresh-form.njk", {
    credentialType,
    authenticated: isAuthenticated(req),
  });
}

export function refreshPostController(req: Request, res: Response): void {
  const { refreshCredential } = req.body;
  const { credentialType } = req.params;

  if (refreshCredential === "true") {
    return res.redirect(`/select-app?credentialType=${credentialType}`);
  }

  if (refreshCredential === "false") {
    return res.redirect(`/refresh/${credentialType}/no-update`);
  }

  return res.render("refresh-form.njk", {
    credentialType,
    authenticated: isAuthenticated(req),
    error: true,
  });
}

export function refreshNoUpdateGetController(
  req: Request,
  res: Response,
): void {
  return res.render("no-update.njk", {
    authenticated: isAuthenticated(req),
  });
}
