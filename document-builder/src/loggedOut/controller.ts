import { Request, Response } from "express";

export function loggedOutGetController(req: Request, res: Response): void {
  res.render("logged-out.njk");
}
