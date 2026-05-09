import { NextFunction, Request, Response } from "express";
import { getSelfUrl } from "../config/appConfig";
import { logger } from "./logger";
import { ROUTES } from "../config/routes";

export function requiresAppSelected(
  req: Request,
  res: Response,
  next: NextFunction,
): void {
  const selectedApp = req.cookies["app"];

  logger.info(`selectedApp: ${selectedApp}`);

  if (selectedApp === undefined) {
    res.redirect(getSelfUrl() + ROUTES.SELECT_APP);
  } else {
    next();
  }
}
