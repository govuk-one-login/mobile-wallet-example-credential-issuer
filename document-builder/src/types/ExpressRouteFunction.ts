import { Request, Response, NextFunction } from "express";

export type ExpressRouteFunction = (
  req: Request,
  res: Response,
  next: NextFunction,
) => Promise<void> | void;
