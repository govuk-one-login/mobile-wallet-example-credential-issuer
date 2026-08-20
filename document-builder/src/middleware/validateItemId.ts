import { NextFunction, Request, Response } from "express";
import { logger } from "./logger";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function validateItemId(
  req: Request,
  _res: Response,
  next: NextFunction,
): void {
  const itemId = req.params.itemId as string;

  if (!itemId || !UUID_REGEX.test(itemId)) {
    logger.error({ itemId }, "Invalid itemId path parameter provided");
    return next(new Error("Invalid itemId path parameter provided"));
  }

  next();
}
