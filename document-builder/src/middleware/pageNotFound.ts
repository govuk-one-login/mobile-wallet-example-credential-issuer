import { NextFunction, Request, Response } from "express";

export function pageNotFound(
  req: Request,
  res: Response,
  next: NextFunction,
): void {
  if (res.headersSent) {
    return next();
  }

  res.status(404);
  res.render("404.njk");
}
