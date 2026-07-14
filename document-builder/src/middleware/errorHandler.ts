import { NextFunction, Request, Response } from "express";
import { logger } from "./logger";

export function errorHandler(
  err: Error,
  req: Request,
  res: Response,
  next: NextFunction,
): void {
  logger.error(err.message);
  res.status(500).render("500.njk");
}