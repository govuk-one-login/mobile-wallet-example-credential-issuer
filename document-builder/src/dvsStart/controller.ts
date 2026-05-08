import { Request, Response } from "express";

export function dvsStartGetController(req: Request, res: Response) {
  res.render("start-page.njk");
}
