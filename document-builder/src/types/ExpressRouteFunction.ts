import { Request, Response } from "express";

export type ExpressRouteFunction = (
  req: Request,
  res: Response,
) => Promise<void> | void;
