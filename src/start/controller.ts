import { Request, Response } from "express";

export function startGetController(req: Request, res: Response) {
  res.render("start-now.njk");
}
